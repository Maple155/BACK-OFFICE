package com.service;

import com.entity.Reservation;
import com.entity.Vehicule;
import com.connect.DatabaseConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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

        // 1. CHERCHER UNE MISSION COMPATIBLE (Pooling existant)
        int missionId = chercherMissionCompatible(res);
        if (missionId != -1) {
            if (tenterAjoutDansMission(missionId, res)) {
                return true; 
            }
        }

        // 2. SINON, CRÉER UNE NOUVELLE MISSION
        return creerNouvelleMission(res);
    }

    /**
     * Vérifie si une mission peut accueillir le client au moment de son arrivée
     */
    private int chercherMissionCompatible(Reservation res) {
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
     * Tente d'ajouter un passager à une mission existante et recalcule le circuit optimisé
     */
    private boolean tenterAjoutDansMission(int missionId, Reservation nouvelleRes) {
        try {
            int vehiculeId = getVehiculeDeMission(missionId);
            List<Reservation> passagersActuels = getReservationsDeLaMission(missionId);
            
            // Vérification capacité
            int placesPrises = passagersActuels.stream().mapToInt(Reservation::getNbPassager).sum();
            if (placesPrises + nouvelleRes.getNbPassager() > getCapaciteVehicule(vehiculeId)) return false;

            // Recalculer le trajet avec le nouveau lieu (Circuit Optimisé)
            List<Integer> lieuxAVisiter = passagersActuels.stream()
                    .map(Reservation::getIdLieu)
                    .collect(Collectors.toList());
            lieuxAVisiter.add(nouvelleRes.getIdLieu());

            double distance = calculerDistanceCircuit(lieuxAVisiter);
            double vitesse = Double.parseDouble(getParametre("VITESSE_MOYENNE_KMH", "40"));
            int dureeTrajet = (int) ((distance / vitesse) * 60);

            LocalDateTime departPrevu = getHeureDepartMission(missionId);
            LocalDateTime nouveauRetour = departPrevu.plusMinutes(dureeTrajet);

            // Vérifier si le nouveau temps de retour n'empiète pas sur la mission suivante du véhicule
            LocalDateTime missionSuivante = getHeureDebutMissionSuivante(vehiculeId, departPrevu);
            if (missionSuivante != null && nouveauRetour.isAfter(missionSuivante)) {
                return false; 
            }

            enregistrerAssignation(missionId, nouvelleRes.getId());
            updateRetourMission(missionId, nouveauRetour);
            return true;
        } catch (Exception e) { return false; }
    }

    /**
     * Crée une mission avec les règles de priorité : Capacité proche > Diesel > Random
     */
    private boolean creerNouvelleMission(Reservation res) {
        // Un véhicule est disponible s'il n'est pas en mission à l'heure de la réservation
        List<Vehicule> vehicules = getVehiculesDisponibles(res.getDateHeure());
        
        // Application des règles 1, 2, 3 et 4
        Vehicule choisi = vehicules.stream()
            .filter(v -> v.getNbPlaces() >= res.getNbPassager())
            .sorted(Comparator.comparingInt((Vehicule v) -> v.getNbPlaces() - res.getNbPassager()) // Règle 2 : plus proche
                .thenComparing((Vehicule v) -> v.getTypeCarburantLibelle().equalsIgnoreCase("Diesel") ? 0 : 1) // Règle 3 : Diesel
                .thenComparing(v -> Math.random())) // Règle 4 : Random
            .findFirst().orElse(null);

        if (choisi != null) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                // RÈGLE 5 : Temps d'attente à partir de la réservation
                int minutesAttente = Integer.parseInt(getParametre("TEMPS_ATTENTE_MIN", "15"));
                LocalDateTime departPrevu = res.getDateHeure().plusMinutes(minutesAttente);
                
                double distance = calculerDistanceCircuit(List.of(res.getIdLieu()));
                double vitesse = Double.parseDouble(getParametre("VITESSE_MOYENNE_KMH", "40"));
                LocalDateTime retourPrevu = departPrevu.plusMinutes((int)((distance/vitesse)*60));

                // L'heure d'arrivée réelle du véhicule à l'aéroport
                LocalDateTime arriveeAero = getHeureArriveeEffective(choisi.getId(), res.getDateHeure());

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

    /**
     * ALGORITHME DU PLUS PROCHE VOISIN (Greedy Circuit)
     * Calcule : Aero -> Lieu A -> Lieu B (le plus proche de A) -> ... -> Aero
     */
    private double calculerDistanceCircuit(List<Integer> lieuxIds) {
        if (lieuxIds == null || lieuxIds.isEmpty()) return 0;
        
        List<Integer> aVisiter = new ArrayList<>(lieuxIds);
        double distanceTotale = 0;
        int pointActuel = AEROPORT_ID;

        

        while (!aVisiter.isEmpty()) {
            final int currentPos = pointActuel;
            // Trouver le lieu restant le plus proche de la position actuelle
            int prochainLieu = aVisiter.stream()
                .min(Comparator.comparingDouble(dest -> getDistance(currentPos, dest)))
                .get();

            distanceTotale += getDistance(pointActuel, prochainLieu);
            pointActuel = prochainLieu;
            aVisiter.remove(Integer.valueOf(prochainLieu));
        }

        // Retour final à l'aéroport
        distanceTotale += getDistance(pointActuel, AEROPORT_ID);
        
        return distanceTotale;
    }

    // --- ACCÈS AUX DONNÉES ---

    private double getDistance(int from, int to) {
        if (from == to) return 0;
        String sql = "SELECT kilometer FROM Distances WHERE (id_from=? AND id_to=?) OR (id_from=? AND id_to=?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, from); pstmt.setInt(2, to);
            pstmt.setInt(3, to);   pstmt.setInt(4, from);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getDouble(1) : 15.0; // 15km par défaut si non trouvé
        } catch (Exception e) { return 15.0; }
    }

    private LocalDateTime getHeureArriveeEffective(int vehiculeId, LocalDateTime heureResa) {
        String sql = "SELECT MAX(heure_retour_prevu) FROM Mission WHERE id_vehicule = ? AND heure_retour_prevu <= ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, vehiculeId);
            pstmt.setTimestamp(2, Timestamp.valueOf(heureResa));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getTimestamp(1) != null) return rs.getTimestamp(1).toLocalDateTime();
        } catch (Exception e) {}
        return heureResa; // Si aucune mission passée, il est considéré dispo à l'heure de la résa
    }

    private List<Vehicule> getVehiculesDisponibles(LocalDateTime t) {
        List<Vehicule> list = new ArrayList<>();
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