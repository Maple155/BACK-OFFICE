package com.service;

import com.entity.Reservation;
import com.entity.Vehicule;
import com.connect.DatabaseConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AssignmentService {

    private static final int AEROPORT_ID = 1;

    /**
     * MÉTHODE PRINCIPALE : Assigne une réservation en cherchant un pooling temporel 
     * ou en créant une nouvelle mission.
     */
    public boolean assignerReservationAutomatiquement(int reservationId) {
        ReservationService resService = new ReservationService();
        Reservation res = resService.getReservationById(reservationId);
        if (res == null) return false;

        // 1. CHERCHER UNE MISSION COMPATIBLE (Pooling)
        // On cherche une mission dont l'heure de départ n'est pas encore passée
        int missionId = chercherMissionCompatible(res);
        if (missionId != -1) {
            if (tenterAjoutDansMission(missionId, res)) {
                return true; 
            }
        }

        // 2. CRÉER UNE NOUVELLE MISSION
        return creerNouvelleMission(res);
    }

    /**
     * CHERCHE UNE MISSION DONT L'HEURE DE DÉPART EST SUPÉRIEURE À L'HEURE DE RÉSERVATION
     */
    private int chercherMissionCompatible(Reservation res) {
        // La mission est compatible si le client arrive pendant que le véhicule 
        // est encore à l'aéroport (entre son arrivée et son départ prévu).
        String sql = "SELECT id FROM Mission " +
                     "WHERE ? >= heure_arrivee_aero " +
                     "AND ? < heure_depart_prevu " +
                     "LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            Timestamp ts = Timestamp.valueOf(res.getDateHeure());
            pstmt.setTimestamp(1, ts);
            pstmt.setTimestamp(2, ts);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt(1) : -1;
        } catch (Exception e) { return -1; }
    }

    /**
     * TENTE D'AJOUTER LE PASSAGER ET RECALCULE LE RETOUR (LOCKING)
     */
    private boolean tenterAjoutDansMission(int missionId, Reservation nouvelleRes) {
        try {
            int vehiculeId = getVehiculeDeMission(missionId);
            List<Reservation> passagersActuels = getReservationsDeLaMission(missionId);
            
            // Vérification capacité véhicule
            int placesPrises = passagersActuels.stream().mapToInt(Reservation::getNbPassager).sum();
            if (placesPrises + nouvelleRes.getNbPassager() > getCapaciteVehicule(vehiculeId)) return false;

            // Recalculer le trajet (Circuit avec le nouveau lieu)
            List<Integer> lieux = new ArrayList<>();
            for (Reservation r : passagersActuels) lieux.add(r.getIdLieu());
            lieux.add(nouvelleRes.getIdLieu());

            double distance = calculerDistanceCircuit(lieux);
            double vitesse = Double.parseDouble(getParametre("VITESSE_MOYENNE_KMH", "40"));
            int dureeTrajet = (int) ((distance / vitesse) * 60);

            LocalDateTime departPrevu = getHeureDepartMission(missionId);
            LocalDateTime nouveauRetour = departPrevu.plusMinutes(dureeTrajet);

            // VÉRIFICATION DU LOCK (La mission suivante est-elle impactée ?)
            LocalDateTime missionSuivante = getHeureDebutMissionSuivante(vehiculeId, departPrevu);
            if (missionSuivante != null && nouveauRetour.isAfter(missionSuivante)) {
                return false; 
            }

            // VALIDATION
            enregistrerAssignation(missionId, nouvelleRes.getId());
            updateRetourMission(missionId, nouveauRetour);
            return true;
        } catch (Exception e) { return false; }
    }

    /**
     * CRÉE UNE MISSION BASÉE SUR LA DISPONIBILITÉ TEMPORELLE
     */
    private boolean creerNouvelleMission(Reservation res) {
        List<Vehicule> vehicules = getVehiculesDisponibles(res.getDateHeure());
        
        // On prend le véhicule le plus adapté (places suffisantes et plus petit gap)
        Vehicule choisi = vehicules.stream()
            .filter(v -> v.getNbPlaces() >= res.getNbPassager())
            .sorted(Comparator.comparingInt((Vehicule v) -> v.getNbPlaces() - res.getNbPassager()))
            .findFirst().orElse(null);

        if (choisi != null) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                LocalDateTime arriveeAero = getHeureArriveeEffective(choisi.getId(), res.getDateHeure());
                int minutesAttente = Integer.parseInt(getParametre("TEMPS_ATTENTE_MIN", "15"));
                LocalDateTime departPrevu = arriveeAero.plusMinutes(minutesAttente);
                
                double distance = calculerDistanceCircuit(List.of(res.getIdLieu()));
                double vitesse = Double.parseDouble(getParametre("VITESSE_MOYENNE_KMH", "40"));
                LocalDateTime retourPrevu = departPrevu.plusMinutes((int)((distance/vitesse)*60));

                String sqlM = "INSERT INTO Mission (id_vehicule, heure_arrivee_aero, heure_depart_prevu, heure_retour_prevu) " +
                              "VALUES (?, ?, ?, ?) RETURNING id";
                PreparedStatement pstmtM = conn.prepareStatement(sqlM, Statement.RETURN_GENERATED_KEYS);
                pstmtM.setInt(1, choisi.getId());
                pstmtM.setTimestamp(2, Timestamp.valueOf(arriveeAero));
                pstmtM.setTimestamp(3, Timestamp.valueOf(departPrevu));
                pstmtM.setTimestamp(4, Timestamp.valueOf(retourPrevu));
                
                pstmtM.execute();
                ResultSet rs = pstmtM.getGeneratedKeys();
                if (rs.next()) {
                    enregistrerAssignation(rs.getInt(1), res.getId());
                    return true;
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
        return false;
    }

    // --- LOGIQUE DE CALCULS ET BDD ---

    private LocalDateTime getHeureArriveeEffective(int vehiculeId, LocalDateTime heureResa) {
        LocalDateTime finDerniere = getFinDerniereMission(vehiculeId, heureResa);
        return (finDerniere == null) ? heureResa : finDerniere;
    }

    private LocalDateTime getFinDerniereMission(int vehiculeId, LocalDateTime avantT) {
        String sql = "SELECT MAX(heure_retour_prevu) FROM Mission WHERE id_vehicule = ? AND heure_retour_prevu <= ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, vehiculeId);
            pstmt.setTimestamp(2, Timestamp.valueOf(avantT));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getTimestamp(1) != null) return rs.getTimestamp(1).toLocalDateTime();
        } catch (Exception e) {}
        return null;
    }

    private List<Vehicule> getVehiculesDisponibles(LocalDateTime t) {
        List<Vehicule> list = new ArrayList<>();
        // Un véhicule est dispo si l'heure T n'est pas dans un intervalle de mission existant
        String sql = "SELECT v.*, tc.libelle as carb FROM Vehicule v " +
                     "JOIN TypeCarburant tc ON v.typeCarburant_id = tc.id " +
                     "WHERE v.id NOT IN (" +
                     "  SELECT id_vehicule FROM Mission " +
                     "  WHERE ? BETWEEN heure_arrivee_aero AND heure_retour_prevu" +
                     ")";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(t));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Vehicule v = new Vehicule();
                v.setId(rs.getInt("id"));
                v.setNbPlaces(rs.getInt("nbPlaces"));
                v.setTypeCarburantLibelle(rs.getString("carb"));
                list.add(v);
            }
        } catch (Exception e) {}
        return list;
    }

    private double calculerDistanceCircuit(List<Integer> lieuxIds) {
        double dist = 0; 
        int current = AEROPORT_ID;
        for (int id : lieuxIds) { 
            dist += getDistance(current, id); 
            current = id; 
        }
        dist += getDistance(current, AEROPORT_ID); // Retour à l'aéroport
        return dist;
    }

    private double getDistance(int from, int to) {
        if (from == to) return 0;
        String sql = "SELECT kilometer FROM Distances WHERE (id_from=? AND id_to=?) OR (id_from=? AND id_to=?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, from); pstmt.setInt(2, to);
            pstmt.setInt(3, to);   pstmt.setInt(4, from);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getDouble(1) : 15.0;
        } catch (Exception e) { return 15.0; }
    }

    private void enregistrerAssignation(int mId, int rId) {
        String sql = "INSERT INTO Vehicules_Reservations (id_mission, id_reservation) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, mId); pstmt.setInt(2, rId);
            pstmt.executeUpdate();
        } catch (Exception e) {}
    }

    private void updateRetourMission(int mId, LocalDateTime retour) {
        String sql = "UPDATE Mission SET heure_retour_prevu = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(retour));
            pstmt.setInt(2, mId);
            pstmt.executeUpdate();
        } catch (Exception e) {}
    }

    private LocalDateTime getHeureDebutMissionSuivante(int vId, LocalDateTime t) {
        String sql = "SELECT MIN(heure_arrivee_aero) FROM Mission WHERE id_vehicule = ? AND heure_arrivee_aero > ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, vId);
            pstmt.setTimestamp(2, Timestamp.valueOf(t));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getTimestamp(1) != null) return rs.getTimestamp(1).toLocalDateTime();
        } catch (Exception e) {}
        return null;
    }

    private List<Reservation> getReservationsDeLaMission(int missionId) {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT r.* FROM Reservation r JOIN Vehicules_Reservations vr ON r.id = vr.id_reservation WHERE vr.id_mission = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, missionId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Reservation r = new Reservation();
                r.setId(rs.getInt("id"));
                r.setIdLieu(rs.getInt("id_lieu"));
                r.setNbPassager(rs.getInt("nbPassager"));
                list.add(r);
            }
        } catch (Exception e) {}
        return list;
    }

    private String getParametre(String code, String def) {
        String sql = "SELECT value FROM Parametres WHERE code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, code);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getString("value") : def;
        } catch (Exception e) { return def; }
    }

    private int getCapaciteVehicule(int id) {
        String sql = "SELECT nbPlaces FROM Vehicule WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) { return 0; }
    }

    private int getVehiculeDeMission(int mId) {
        String sql = "SELECT id_vehicule FROM Mission WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, mId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt(1) : -1;
        } catch (Exception e) { return -1; }
    }

    private LocalDateTime getHeureDepartMission(int mId) {
        String sql = "SELECT heure_depart_prevu FROM Mission WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, mId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getTimestamp(1).toLocalDateTime() : null;
        } catch (Exception e) { return null; }
    }
}