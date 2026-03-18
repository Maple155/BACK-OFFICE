package com.service;

import com.entity.Reservation;
import com.entity.Vehicule;
import com.entity.Lieu;
import com.connect.DatabaseConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AssignmentService {

    private static final int AEROPORT_ID = 1;

    /**
     * RÈGLE DE GESTION : Ordonnancement du traitement des réservations.
     * 1 - Les plus tôt d'abord (Chronologique).
     * 2 - Si même heure, le plus de passagers d'abord (Priorité volume).
     */
    public void traiterReservationsEnAttente(LocalDateTime debut, LocalDateTime fin) {
        ReservationService resService = new ReservationService();
        System.out.println("--- DÉBUT DU TRAITEMENT ---");

        boolean changement = true;
        int passe = 1;
        while (changement && passe <= 10) {
            changement = false;

            List<Reservation> enAttente = resService.getUnassignedReservationsByDateTimeRange(debut, fin);
            if (enAttente.isEmpty()) {
                break;
            }

            List<Reservation> triee = enAttente.stream()
                .sorted(Comparator.comparing(Reservation::getDateHeure)
                    .thenComparing(Comparator.comparingInt(Reservation::getNbPassager).reversed()))
                .collect(Collectors.toList());

            System.out.println("--- PASSE " + passe + " | En attente: " + triee.size() + " ---");
            for (Reservation res : triee) {
                System.out.println(">>> Traitement Res ID: " + res.getId() + " | Passagers: " + res.getNbPassager() + " | Heure: " + res.getDateHeure());
                if (assignerReservationAutomatiquement(res.getId())) {
                    changement = true;
                }
            }

            passe++;
        }

        System.out.println("--- FIN DU TRAITEMENT ---");
    }

    public boolean assignerReservationAutomatiquement(int reservationId) {
        ReservationService resService = new ReservationService();
        Reservation res = resService.getReservationById(reservationId);
        if (res == null) return false;

        // 1) Priorité: tenter d'ajouter la réservation dans une mission de la même vague
        // encore "ouverte" (départ pas encore passé) et avec capacité disponible.
        int missionId = chercherMissionCompatible(res);
        
        if (missionId != -1) {
            // On réutilise tenterAjoutDansMission pour recalculer le trajet/distance
            if (tenterAjoutDansMission(missionId, res)) {
                LocalDateTime vague = getHeureArriveeAeroMission(missionId);
                if (vague == null) {
                    vague = res.getDateHeure();
                }
                synchroniserDepartEtRetoursVague(vague);
                return true; 
            }
        }

        // 2) Sinon, créer une nouvelle mission en choisissant le meilleur véhicule disponible.
        boolean cree = creerNouvelleMission(res);
        if (cree) {
            // Synchronise la vague réellement active pour l'heure de cette réservation.
            synchroniserDepartEtRetoursVague(determinerAncreVague(res.getDateHeure()));
        }
        return cree;
    }

    private int chercherMissionCompatible(Reservation res) {
        int minutesAttente = Integer.parseInt(getParametre("TEMPS_ATTENTE_MIN", "15"));
        // Mission compatible si:
        // - la vague n'est pas expirée pour cette réservation
        //   (permet aussi à une réservation backlog plus ancienne de rejoindre une vague suivante)
        // - il reste assez de places
        // Parmi les candidates: on prend la mission la plus "remplie" possible (moins de places libres).
        String sql = 
            "SELECT m.id, (v.nbPlaces - COALESCE(SUM(r.nbPassager), 0)) as places_libres " +
            "FROM Mission m " +
            "JOIN Vehicule v ON m.id_vehicule = v.id " +
            "LEFT JOIN Vehicules_Reservations vr ON m.id = vr.id_mission " +
            "LEFT JOIN Reservation r ON vr.id_reservation = r.id " +
            "WHERE ? <= (m.heure_arrivee_aero + (? * INTERVAL '1 minute')) " +
            "GROUP BY m.id, v.nbPlaces " +
            "HAVING (v.nbPlaces - COALESCE(SUM(r.nbPassager), 0)) >= ? " + 
            "ORDER BY places_libres ASC, m.id ASC"; // Ajout de m.id ASC pour la stabilité

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            Timestamp ts = Timestamp.valueOf(res.getDateHeure());
            pstmt.setTimestamp(1, ts);
            pstmt.setInt(2, minutesAttente);
            pstmt.setInt(3, res.getNbPassager());

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (Exception e) { e.printStackTrace(); }
        return -1;
    }

    private boolean tenterAjoutDansMission(int missionId, Reservation nouvelleRes) {
        try {
            int vehiculeId = getVehiculeDeMission(missionId);
            List<Reservation> passagersActuels = getReservationsDeLaMission(missionId);
            
            int placesPrises = passagersActuels.stream().mapToInt(Reservation::getNbPassager).sum();
            if (placesPrises + nouvelleRes.getNbPassager() > getCapaciteVehicule(vehiculeId)) return false;

            List<Integer> lieuxAVisiter = passagersActuels.stream().map(Reservation::getIdLieu).collect(Collectors.toList());
            lieuxAVisiter.add(nouvelleRes.getIdLieu());

            double distance = calculerDistanceCircuit(lieuxAVisiter);
            double vitesse = Double.parseDouble(getParametre("VITESSE_MOYENNE_KMH", "40"));
            LocalDateTime departPrevu = getHeureDepartMission(missionId);
            LocalDateTime nouveauRetour = departPrevu.plusMinutes((int)((distance / vitesse) * 60));

            LocalDateTime missionSuivante = getHeureDebutMissionSuivante(vehiculeId, departPrevu);
            if (missionSuivante != null && nouveauRetour.isAfter(missionSuivante)) return false; 

            enregistrerAssignation(missionId, nouvelleRes.getId());
            updateRetourMission(missionId, nouveauRetour);
            return true;
        } catch (Exception e) { return false; }
    }

    private boolean creerNouvelleMission(Reservation res) {
        Set<LocalDateTime> ancres = new LinkedHashSet<>();
        ancres.addAll(getAncresVaguesCompatibles(res.getDateHeure()));
        ancres.add(determinerAncreVague(res.getDateHeure()));
        ancres.add(res.getDateHeure());

        for (LocalDateTime ancreVague : ancres) {
            if (essayerCreerMissionSurVague(res, ancreVague)) {
                return true;
            }
        }
        return false;
    }

    private boolean essayerCreerMissionSurVague(Reservation res, LocalDateTime ancreVague) {
        int minutesAttente = Integer.parseInt(getParametre("TEMPS_ATTENTE_MIN", "15"));
        LocalDateTime finVague = ancreVague.plusMinutes(minutesAttente);

        List<Vehicule> candidats = new ArrayList<>();
        Map<Integer, Integer> trajetsParVehicule = new HashMap<>();
        Map<Integer, LocalDateTime> disponibiliteParVehicule = new HashMap<>();

        String sqlVehicules = "SELECT v.*, tc.libelle as carb FROM Vehicule v " +
                             "JOIN TypeCarburant tc ON v.typeCarburant_id = tc.id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlVehicules);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Vehicule v = new Vehicule();
                v.setId(rs.getInt("id"));
                v.setNbPlaces(rs.getInt("nbPlaces"));
                v.setTypeCarburantLibelle(rs.getString("carb"));

                LocalDateTime disponibilite = getHeureDisponibiliteVehiculePourVague(v.getId(), ancreVague, finVague);
                if (disponibilite != null
                    && !disponibilite.isAfter(finVague)
                    && v.getNbPlaces() >= res.getNbPassager()
                    && !vehiculeDejaAffecteDansVague(v.getId(), ancreVague)) {
                    candidats.add(v);
                    disponibiliteParVehicule.put(v.getId(), disponibilite);
                    trajetsParVehicule.put(v.getId(), getNombreTrajetsVehiculePourJour(v.getId(), ancreVague));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        int totalPassagersVague = getTotalPassagersNonAssignesDansFenetre(ancreVague, finVague);
        int capaciteCible = totalPassagersVague > 0 ? totalPassagersVague : res.getNbPassager();

        List<Vehicule> candidatsTries = candidats.stream()
            .sorted(Comparator
                .comparingInt((Vehicule v) -> trajetsParVehicule.getOrDefault(v.getId(), 0))
                .thenComparing((Vehicule v) -> v.getNbPlaces() >= capaciteCible ? 0 : 1)
                .thenComparingInt(v -> Math.abs(v.getNbPlaces() - res.getNbPassager()))
                .thenComparing((Vehicule v) -> v.getTypeCarburantLibelle().equalsIgnoreCase("Diesel") ? 0 : 1)
                .thenComparingInt(Vehicule::getId))
            .collect(Collectors.toList());

        for (Vehicule choisi : candidatsTries) {
            LocalDateTime disponibilite = disponibiliteParVehicule.getOrDefault(choisi.getId(), ancreVague);
            LocalDateTime departPrevu = disponibilite.isAfter(ancreVague) ? disponibilite : ancreVague;
            if (departPrevu.isAfter(finVague)) {
                continue;
            }

            double distance = calculerDistanceCircuit(List.of(res.getIdLieu()));
            double vitesse = Double.parseDouble(getParametre("VITESSE_MOYENNE_KMH", "40"));
            LocalDateTime retourPrevu = departPrevu.plusMinutes((int)((distance / vitesse) * 60));

            if (hasChevauchementMissionVehicule(choisi.getId(), departPrevu, retourPrevu)) {
                continue;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                String sqlM = "INSERT INTO Mission (id_vehicule, heure_arrivee_aero, heure_depart_prevu, heure_retour_prevu) VALUES (?, ?, ?, ?) RETURNING id";
                PreparedStatement pstmtM = conn.prepareStatement(sqlM, Statement.RETURN_GENERATED_KEYS);
                pstmtM.setInt(1, choisi.getId());
                pstmtM.setTimestamp(2, Timestamp.valueOf(ancreVague));
                pstmtM.setTimestamp(3, Timestamp.valueOf(departPrevu));
                pstmtM.setTimestamp(4, Timestamp.valueOf(retourPrevu));

                pstmtM.execute();
                ResultSet rs = pstmtM.getGeneratedKeys();
                if (rs.next()) {
                    enregistrerAssignation(rs.getInt(1), res.getId());
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    private void synchroniserDepartEtRetoursVague(LocalDateTime debutVague) {
        int minutesAttente = Integer.parseInt(getParametre("TEMPS_ATTENTE_MIN", "15"));
        LocalDateTime finAttenteTheorique = debutVague.plusMinutes(minutesAttente);

        List<Integer> missionIds = getMissionIdsDeVague(debutVague);
        if (missionIds.isEmpty()) {
            return;
        }

        double vitesse = Double.parseDouble(getParametre("VITESSE_MOYENNE_KMH", "40"));
        Map<Integer, List<Integer>> lieuxParMission = new HashMap<>();
        LocalDateTime disponibiliteMaxVague = debutVague;

        for (Integer missionId : missionIds) {
            List<Reservation> reservations = getReservationsDeLaMission(missionId);
            if (reservations.isEmpty()) {
                continue;
            }

            int vehiculeId = getVehiculeDeMission(missionId);
            LocalDateTime disponibiliteVehicule = getHeureDisponibiliteVehiculePourVague(vehiculeId, debutVague, finAttenteTheorique, missionId);
            if (disponibiliteVehicule == null || disponibiliteVehicule.isAfter(finAttenteTheorique)) {
                continue;
            }

            if (disponibiliteVehicule.isAfter(disponibiliteMaxVague)) {
                disponibiliteMaxVague = disponibiliteVehicule;
            }

            List<Integer> lieux = reservations.stream().map(Reservation::getIdLieu).collect(Collectors.toList());
            lieuxParMission.put(missionId, lieux);
        }

        if (lieuxParMission.isEmpty()) {
            return;
        }

        // Départ commun de la vague:
        // - au moins la dernière réservation observée dans la vague
        // - au moins la dispo la plus tardive des véhicules de cette vague
        LocalDateTime derniereReservationVague = getDerniereReservationDeVagueAvantOuEgale(debutVague, finAttenteTheorique);
        LocalDateTime baseVague = (derniereReservationVague != null && derniereReservationVague.isAfter(debutVague))
            ? derniereReservationVague
            : debutVague;

        LocalDateTime departCommun = disponibiliteMaxVague.isAfter(baseVague)
            ? disponibiliteMaxVague
            : baseVague;

        if (departCommun.isAfter(finAttenteTheorique)) {
            departCommun = finAttenteTheorique;
        }

        for (Map.Entry<Integer, List<Integer>> entry : lieuxParMission.entrySet()) {
            Integer missionId = entry.getKey();
            List<Integer> lieux = entry.getValue();
            double distance = calculerDistanceCircuit(lieux);
            LocalDateTime retour = departCommun.plusMinutes((int)((distance / vitesse) * 60));
            updateMissionDepartEtRetour(missionId, departCommun, retour);
        }
    }

    private LocalDateTime determinerAncreVague(LocalDateTime dateHeureReservation) {
        int minutesAttente = Integer.parseInt(getParametre("TEMPS_ATTENTE_MIN", "15"));
        String sql = "SELECT MIN(heure_arrivee_aero) " +
                     "FROM Mission " +
                     "WHERE ? <= (heure_arrivee_aero + (? * INTERVAL '1 minute'))";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            Timestamp ts = Timestamp.valueOf(dateHeureReservation);
            pstmt.setTimestamp(1, ts);
            pstmt.setInt(2, minutesAttente);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getTimestamp(1) != null) {
                return rs.getTimestamp(1).toLocalDateTime();
            }
        } catch (Exception e) {}
        return dateHeureReservation;
    }

    private List<LocalDateTime> getAncresVaguesCompatibles(LocalDateTime dateHeureReservation) {
        List<LocalDateTime> ancres = new ArrayList<>();
        int minutesAttente = Integer.parseInt(getParametre("TEMPS_ATTENTE_MIN", "15"));
        String sql = "SELECT DISTINCT heure_arrivee_aero " +
                     "FROM Mission " +
                     "WHERE ? <= (heure_arrivee_aero + (? * INTERVAL '1 minute')) " +
                     "ORDER BY heure_arrivee_aero ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(dateHeureReservation));
            pstmt.setInt(2, minutesAttente);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Timestamp ts = rs.getTimestamp(1);
                if (ts != null) {
                    ancres.add(ts.toLocalDateTime());
                }
            }
        } catch (Exception e) {}
        return ancres;
    }

    private boolean vehiculeDejaAffecteDansVague(int vehiculeId, LocalDateTime ancreVague) {
        String sql = "SELECT 1 FROM Mission WHERE id_vehicule = ? AND heure_arrivee_aero = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, vehiculeId);
            pstmt.setTimestamp(2, Timestamp.valueOf(ancreVague));
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean hasChevauchementMissionVehicule(int vehiculeId, LocalDateTime debut, LocalDateTime fin) {
        String sql = "SELECT 1 FROM Mission " +
                     "WHERE id_vehicule = ? " +
                     "AND NOT (heure_retour_prevu <= ? OR heure_depart_prevu >= ?) " +
                     "LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, vehiculeId);
            pstmt.setTimestamp(2, Timestamp.valueOf(debut));
            pstmt.setTimestamp(3, Timestamp.valueOf(fin));
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * CIRCUIT OPTIMISÉ (Greedy)
     * RÈGLE : Si distance égale entre deux lieux, tri alphabétique.
     */
    private double calculerDistanceCircuit(List<Integer> lieuxIds) {
        if (lieuxIds == null || lieuxIds.isEmpty()) return 0;
        
        List<Lieu> aVisiter = lieuxIds.stream().distinct().map(this::getLieuById).collect(Collectors.toCollection(ArrayList::new));
        double distanceTotale = 0;
        int pointActuelId = AEROPORT_ID;

        

        while (!aVisiter.isEmpty()) {
            final int currentId = pointActuelId;
            Lieu prochainLieu = aVisiter.stream()
                .min((l1, l2) -> {
                    double d1 = getDistance(currentId, l1.getId());
                    double d2 = getDistance(currentId, l2.getId());
                    if (Math.abs(d1 - d2) < 0.001) { 
                        return l1.getLibelle().compareToIgnoreCase(l2.getLibelle());
                    }
                    return Double.compare(d1, d2);
                }).get();

            distanceTotale += getDistance(pointActuelId, prochainLieu.getId());
            pointActuelId = prochainLieu.getId();
            aVisiter.remove(prochainLieu);
        }

        distanceTotale += getDistance(pointActuelId, AEROPORT_ID); 
        return distanceTotale;
    }

    // --- ACCÈS AUX DONNÉES ET UTILITAIRES ---

    private Lieu getLieuById(int id) {
        String sql = "SELECT * FROM Lieu WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Lieu l = new Lieu();
                l.setId(rs.getInt("id"));
                l.setLibelle(rs.getString("libelle"));
                return l;
            }
        } catch (Exception e) {}
        return null;
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

    private LocalDateTime getHeureArriveeEffective(int vId, LocalDateTime h) {
        return getHeureArriveeEffective(vId, h, -1);
    }

    private LocalDateTime getHeureArriveeEffective(int vId, LocalDateTime h, int missionIdExclue) {
        String sql = "SELECT MAX(heure_retour_prevu) FROM Mission WHERE id_vehicule = ? AND heure_retour_prevu <= ? AND id <> ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, vId);
            pstmt.setTimestamp(2, Timestamp.valueOf(h));
            pstmt.setInt(3, missionIdExclue);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getTimestamp(1) != null) return rs.getTimestamp(1).toLocalDateTime();
        } catch (Exception e) {}
        return null;
    }

    private List<Vehicule> getVehiculesDisponibles(LocalDateTime t) {
        List<Vehicule> list = new ArrayList<>();
        String sql = "SELECT v.*, tc.libelle as carb FROM Vehicule v " +
                     "JOIN TypeCarburant tc ON v.typeCarburant_id = tc.id " +
                     "WHERE v.id NOT IN (SELECT id_vehicule FROM Mission WHERE ? BETWEEN heure_arrivee_aero AND heure_retour_prevu)";
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

    private LocalDateTime getHeureDisponibiliteVehiculePourVague(int vehiculeId, LocalDateTime debutVague, LocalDateTime finVague) {
        return getHeureDisponibiliteVehiculePourVague(vehiculeId, debutVague, finVague, -1);
    }

    private LocalDateTime getHeureDisponibiliteVehiculePourVague(int vehiculeId, LocalDateTime debutVague, LocalDateTime finVague, int missionIdExclue) {
        // Si le véhicule est en mission au début de la vague, sa disponibilité = heure_retour_prevu.
        String sqlMissionEnCours = "SELECT heure_retour_prevu FROM Mission " +
                                   "WHERE id_vehicule = ? AND id <> ? AND heure_arrivee_aero <= ? AND heure_retour_prevu > ? " +
                                   "ORDER BY heure_retour_prevu ASC LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlMissionEnCours)) {
            pstmt.setInt(1, vehiculeId);
            pstmt.setInt(2, missionIdExclue);
            pstmt.setTimestamp(3, Timestamp.valueOf(debutVague));
            pstmt.setTimestamp(4, Timestamp.valueOf(debutVague));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getTimestamp(1) != null) {
                return rs.getTimestamp(1).toLocalDateTime();
            }
        } catch (Exception e) {}

        // Sinon, on prend son dernier retour avant la fin de vague (si existant),
        // ou le début de vague s'il n'a pas encore roulé ce jour-là.
        LocalDateTime retourAvantFin = getHeureArriveeEffective(vehiculeId, finVague, missionIdExclue);
        if (retourAvantFin == null || retourAvantFin.isBefore(debutVague)) {
            return debutVague;
        }
        return retourAvantFin;
    }

    private int getNombreTrajetsVehiculePourJour(int vehiculeId, LocalDateTime jourReference) {
        // Nombre de missions commencées le même jour: utilisé pour équilibrer la répartition.
        String sql = "SELECT COUNT(*) FROM Mission WHERE id_vehicule = ? AND DATE(heure_arrivee_aero) = DATE(?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, vehiculeId);
            pstmt.setTimestamp(2, Timestamp.valueOf(jourReference));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {}
        return 0;
    }

    private int getTotalPassagersNonAssignesDansFenetre(LocalDateTime debutFenetre, LocalDateTime finFenetre) {
        // Somme des passagers non assignés sur la fenêtre de vague courante.
        // Sert à détecter le cas "un seul véhicule peut contenir toute la vague".
        String sql = "SELECT COALESCE(SUM(r.nbPassager), 0) " +
                     "FROM Reservation r " +
                     "LEFT JOIN Vehicules_Reservations vr ON r.id = vr.id_reservation " +
                     "WHERE vr.id_reservation IS NULL AND r.dateHeure >= ? AND r.dateHeure <= ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(debutFenetre));
            pstmt.setTimestamp(2, Timestamp.valueOf(finFenetre));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {}
        return 0;
    }

    private LocalDateTime getHeureArriveeAeroMission(int missionId) {
        String sql = "SELECT heure_arrivee_aero FROM Mission WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, missionId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getTimestamp(1) != null) {
                return rs.getTimestamp(1).toLocalDateTime();
            }
        } catch (Exception e) {}
        return null;
    }

    private void enregistrerAssignation(int mId, int rId) {
        String sql = "INSERT INTO Vehicules_Reservations (id_mission, id_reservation) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, mId); pstmt.setInt(2, rId);
            pstmt.executeUpdate();
        } catch (Exception e) {}
    }

    private void updateRetourMission(int mId, LocalDateTime r) {
        String sql = "UPDATE Mission SET heure_retour_prevu = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(r));
            pstmt.setInt(2, mId);
            pstmt.executeUpdate();
        } catch (Exception e) {}
    }

    private void updateMissionDepartEtRetour(int mId, LocalDateTime depart, LocalDateTime retour) {
        String sql = "UPDATE Mission SET heure_depart_prevu = ?, heure_retour_prevu = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(depart));
            pstmt.setTimestamp(2, Timestamp.valueOf(retour));
            pstmt.setInt(3, mId);
            pstmt.executeUpdate();
        } catch (Exception e) {}
    }

    private LocalDateTime getDebutVagueDuJour(LocalDateTime reference) {
        String sql = "SELECT MIN(heure_arrivee_aero) FROM Mission WHERE DATE(heure_arrivee_aero) = DATE(?)";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(reference));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getTimestamp(1) != null) return rs.getTimestamp(1).toLocalDateTime();
        } catch (Exception e) {}
        return null;
    }

    private LocalDateTime getDerniereReservationDeVagueAvantOuEgale(LocalDateTime debut, LocalDateTime finTheorique) {
        String sql = "SELECT MAX(r.dateHeure) " +
                     "FROM Reservation r " +
                     "JOIN Vehicules_Reservations vr ON r.id = vr.id_reservation " +
                     "JOIN Mission m ON m.id = vr.id_mission " +
                     "WHERE m.heure_arrivee_aero = ? AND r.dateHeure <= ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(debut));
            pstmt.setTimestamp(2, Timestamp.valueOf(finTheorique));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getTimestamp(1) != null) return rs.getTimestamp(1).toLocalDateTime();
        } catch (Exception e) {}
        return null;
    }

    private LocalDateTime getDerniereReservationDeMissionAvantOuEgale(int missionId, LocalDateTime finTheorique) {
        String sql = "SELECT MAX(r.dateHeure) " +
                     "FROM Reservation r " +
                     "JOIN Vehicules_Reservations vr ON r.id = vr.id_reservation " +
                     "WHERE vr.id_mission = ? AND r.dateHeure <= ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, missionId);
            pstmt.setTimestamp(2, Timestamp.valueOf(finTheorique));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getTimestamp(1) != null) {
                return rs.getTimestamp(1).toLocalDateTime();
            }
        } catch (Exception e) {}
        return null;
    }

    private List<Integer> getMissionIdsDeVague(LocalDateTime debutVague) {
        List<Integer> missionIds = new ArrayList<>();
        String sql = "SELECT id FROM Mission WHERE heure_arrivee_aero = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(debutVague));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                missionIds.add(rs.getInt("id"));
            }
        } catch (Exception e) {}
        return missionIds;
    }

    private LocalDateTime getHeureDebutMissionSuivante(int vId, LocalDateTime t) {
        String sql = "SELECT MIN(heure_arrivee_aero) FROM Mission WHERE id_vehicule = ? AND heure_arrivee_aero > ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, vId);
            pstmt.setTimestamp(2, Timestamp.valueOf(t));
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getTimestamp(1) != null) return rs.getTimestamp(1).toLocalDateTime();
        } catch (Exception e) {}
        return null;
    }

    private List<Reservation> getReservationsDeLaMission(int mId) {
        List<Reservation> list = new ArrayList<>();
        String sql = "SELECT r.* FROM Reservation r JOIN Vehicules_Reservations vr ON r.id = vr.id_reservation WHERE vr.id_mission = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, mId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Reservation r = new Reservation();
                r.setId(rs.getInt("id"));
                r.setIdLieu(rs.getInt("id_lieu"));
                r.setNbPassager(rs.getInt("nbPassager"));
                r.setDateHeure(rs.getTimestamp("dateHeure").toLocalDateTime());
                list.add(r);
            }
        } catch (Exception e) {}
        return list;
    }

    private String getParametre(String c, String d) {
        String sql = "SELECT value FROM Parametres WHERE code = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, c);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getString("value") : d;
        } catch (Exception e) { return d; }
    }

    private int getCapaciteVehicule(int id) {
        String sql = "SELECT nbPlaces FROM Vehicule WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } catch (Exception e) { return 0; }
    }

    private int getVehiculeDeMission(int mId) {
        String sql = "SELECT id_vehicule FROM Mission WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, mId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getInt(1) : -1;
        } catch (Exception e) { return -1; }
    }

    private LocalDateTime getHeureDepartMission(int mId) {
        String sql = "SELECT heure_depart_prevu FROM Mission WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, mId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getTimestamp(1).toLocalDateTime() : null;
        } catch (Exception e) { return null; }
    }
}