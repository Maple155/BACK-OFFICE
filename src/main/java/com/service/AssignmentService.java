package com.service;

import com.entity.Reservation;
import com.entity.Vehicule;
import com.entity.Lieu;
import com.connect.DatabaseConnection;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
class MissionCapacite {
    int id;
    int placesLibres;

    public MissionCapacite(int id, int placesLibres) {
        this.id = id;
        this.placesLibres = placesLibres;
    }
}

class ReservationAVider {
    Reservation reservation;
    int nbPassagersRestants;

    ReservationAVider(Reservation reservation, int nbPassagersRestants) {
        this.reservation = reservation;
        this.nbPassagersRestants = nbPassagersRestants;
    }
}

public class AssignmentService {

    private static final int AEROPORT_ID = 1;
    private List<LocalDateTime> ancres = new ArrayList<>();
    private final List<MissionCapacite> VehiculesNonVideARemplir = new ArrayList<>();
    private final List<ReservationAVider> ReservationAVider = new ArrayList<>();
    /**
     * RÈGLE DE GESTION : Ordonnancement du traitement des réservations.
     * 1 - Les plus tôt d'abord (Chronologique).
     * 2 - Si même heure, le plus de passagers d'abord (Priorité volume).
     */
    public void traiterReservationsEnAttente(LocalDateTime debut, LocalDateTime fin) {
        this.ancres.clear();
        ReservationService resService = new ReservationService();
        
        // 1. Récupération du paramètre de fenêtre (ex: 15 min)
        int tempsAttente = Integer.parseInt(getParametre("TEMPS_ATTENTE_MIN", "15"));
        
        // On récupère le backlog initial pour identifier les points d'ancrage (les vagues)
        List<Reservation> initialBacklog = resService.getUnassignedReservationsByDateTimeRange(debut, fin);
        if (initialBacklog.isEmpty()) return;

        // 2. Identification chronologique des ancres de vagues
        initialBacklog.sort(Comparator.comparing(Reservation::getDateHeure));
        List<LocalDateTime> vaguesAncres = new ArrayList<>();
        for (Reservation res : initialBacklog) {
            boolean dansUneVague = false;
            for (LocalDateTime ancre : vaguesAncres) {
                if (!res.getDateHeure().isBefore(ancre) && !res.getDateHeure().isAfter(ancre.plusMinutes(tempsAttente))) {
                    dansUneVague = true;
                    break;
                }
            }
            if (!dansUneVague) {
                vaguesAncres.add(res.getDateHeure());
            }
        }
        this.ancres.addAll(vaguesAncres);

        // 3. TRAITEMENT PAR FLUX (Vague par vague)
        for (LocalDateTime ancre : vaguesAncres) {
            LocalDateTime finFenetreVague = ancre.plusMinutes(tempsAttente);
            System.out.println("\n--- TRAITEMENT VAGUE : " + ancre + " ---");

            List<Reservation> aTraiterMaintenant = resService.getUnassignedBeforeOrAt(finFenetreVague);
            if (aTraiterMaintenant.isEmpty()) {
                continue;
            }

            aTraiterMaintenant.sort(Comparator
                    .comparing(Reservation::getDateHeure)
                    .thenComparing(Comparator.comparingInt(Reservation::getNbPassager).reversed()));

            this.VehiculesNonVideARemplir.clear();
            this.VehiculesNonVideARemplir.addAll(getMissionsOuvertesDeLaVague(ancre, finFenetreVague));

            this.ReservationAVider.clear();
            for (Reservation reservation : aTraiterMaintenant) {
                this.ReservationAVider.add(new ReservationAVider(reservation, reservation.getNbPassager()));
            }

            traiterFluxPrioritaire(ancre, finFenetreVague);

            // Une fois la vague finie, on aligne les horaires de départ de tous les véhicules de cette ancre
            synchroniserDepartEtRetoursVague(ancre);
        }
    }

    private void traiterFluxPrioritaire(LocalDateTime ancreVague, LocalDateTime finVague) {
        while (!this.ReservationAVider.isEmpty()) {
            boolean progression = false;

            // 1) Priorité aux véhicules non vides à remplir.
            Iterator<MissionCapacite> missionsIterator = this.VehiculesNonVideARemplir.iterator();
            while (missionsIterator.hasNext()) {
                MissionCapacite mission = missionsIterator.next();
                if (mission.placesLibres <= 0) {
                    missionsIterator.remove();
                    continue;
                }

                ReservationAVider reservationCandidate = trouverReservationPlusProche(mission.placesLibres);
                if (reservationCandidate == null) {
                    continue;
                }

                int nbPris = Math.min(mission.placesLibres, reservationCandidate.nbPassagersRestants);
                enregistrerAssignationPartielle(mission.id, reservationCandidate.reservation.getId(), nbPris);

                mission.placesLibres -= nbPris;
                reservationCandidate.nbPassagersRestants -= nbPris;
                progression = true;

                if (reservationCandidate.nbPassagersRestants <= 0) {
                    this.ReservationAVider.remove(reservationCandidate);
                }
                if (mission.placesLibres <= 0) {
                    missionsIterator.remove();
                }
            }

            if (this.ReservationAVider.isEmpty()) {
                break;
            }

            // 2) Priorité à la réservation partiellement vide à terminer.
            ReservationAVider reservationCourante = this.ReservationAVider.get(0);
            Vehicule vehiculeChoisi = chercherVehiculePourReservationAVider(ancreVague, finVague, reservationCourante.nbPassagersRestants);

            if (vehiculeChoisi == null) {
                // Aucun véhicule disponible sur cette vague.
                // On ne crée un reliquat que si la réservation a déjà été partiellement vidée.
                if (reservationCourante.nbPassagersRestants < reservationCourante.reservation.getNbPassager()) {
                    dupliquerReservationPourReliquat(reservationCourante.reservation, reservationCourante.nbPassagersRestants);
                    this.ReservationAVider.remove(0);
                    progression = true;
                }
            } else {
                int missionId = creerMissionVide(vehiculeChoisi.getId(), ancreVague);
                if (missionId != -1) {
                    int nbPris = Math.min(vehiculeChoisi.getNbPlaces(), reservationCourante.nbPassagersRestants);
                    enregistrerAssignationPartielle(missionId, reservationCourante.reservation.getId(), nbPris);
                    reservationCourante.nbPassagersRestants -= nbPris;
                    progression = true;

                    int placesRestantesMission = vehiculeChoisi.getNbPlaces() - nbPris;
                    if (placesRestantesMission > 0) {
                        this.VehiculesNonVideARemplir.add(new MissionCapacite(missionId, placesRestantesMission));
                    }

                    if (reservationCourante.nbPassagersRestants <= 0) {
                        this.ReservationAVider.remove(0);
                    }
                }
            }

            if (!progression) {
                break;
            }
        }

        for (ReservationAVider reste : new ArrayList<>(this.ReservationAVider)) {
            // Reliquat uniquement si la réservation a été partiellement traitée.
            if (reste.nbPassagersRestants > 0 && reste.nbPassagersRestants < reste.reservation.getNbPassager()) {
                dupliquerReservationPourReliquat(reste.reservation, reste.nbPassagersRestants);
            }
        }
        this.ReservationAVider.clear();
    }

    private ReservationAVider trouverReservationPlusProche(int nbPlacesRestantes) {
        return this.ReservationAVider.stream()
            .min(Comparator
                .comparingInt((ReservationAVider r) -> Math.abs(r.nbPassagersRestants - nbPlacesRestantes))
                .thenComparing((ReservationAVider r) -> r.nbPassagersRestants > nbPlacesRestantes ? 0 : 1)
                .thenComparing(r -> r.reservation.getDateHeure())
                .thenComparingInt(r -> r.reservation.getId()))
            .orElse(null);
    }

    private Vehicule chercherVehiculePourReservationAVider(LocalDateTime ancre, LocalDateTime fin, int nbPassagersRestants) {
        List<Vehicule> candidats = new ArrayList<>();
        String sql = "SELECT v.*, tc.libelle as carb FROM Vehicule v " +
                    "JOIN TypeCarburant tc ON v.typeCarburant_id = tc.id";

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int vId = rs.getInt("id");
                LocalDateTime dispo = getHeureDisponibiliteVehiculePourVague(vId, ancre, fin);
                if (dispo != null
                    && !dispo.isAfter(fin)
                    && !vehiculeDejaAffecteDansVague(vId, ancre)) {
                    Vehicule v = new Vehicule();
                    v.setId(vId);
                    v.setNbPlaces(rs.getInt("nbPlaces"));
                    v.setTypeCarburantLibelle(rs.getString("carb"));
                    Time heureDebutDispo = rs.getTime("heure_debut_disponibilite");
                    v.setHeureDebutDisponibilite(heureDebutDispo != null ? heureDebutDispo.toLocalTime() : LocalTime.MIDNIGHT);
                    candidats.add(v);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (candidats.isEmpty()) {
            return null;
        }

        return candidats.stream().min((v1, v2) -> {
            boolean v1AssezGrand = v1.getNbPlaces() >= nbPassagersRestants;
            boolean v2AssezGrand = v2.getNbPlaces() >= nbPassagersRestants;

            if (v1AssezGrand && !v2AssezGrand) return -1;
            if (!v1AssezGrand && v2AssezGrand) return 1;

            int diff1 = Math.abs(v1.getNbPlaces() - nbPassagersRestants);
            int diff2 = Math.abs(v2.getNbPlaces() - nbPassagersRestants);
            if (diff1 != diff2) return Integer.compare(diff1, diff2);

            if (v1AssezGrand && v2AssezGrand) {
                return Integer.compare(v1.getNbPlaces(), v2.getNbPlaces());
            }

            int compTaille = Integer.compare(v2.getNbPlaces(), v1.getNbPlaces());
            if (compTaille != 0) return compTaille;

            int m1 = getNombreTrajetsVehiculePourJour(v1.getId(), ancre);
            int m2 = getNombreTrajetsVehiculePourJour(v2.getId(), ancre);
            if (m1 != m2) return Integer.compare(m1, m2);

            if (v1.getTypeCarburantLibelle().equalsIgnoreCase("Diesel") && !v2.getTypeCarburantLibelle().equalsIgnoreCase("Diesel")) return -1;
            if (!v1.getTypeCarburantLibelle().equalsIgnoreCase("Diesel") && v2.getTypeCarburantLibelle().equalsIgnoreCase("Diesel")) return 1;

            return Integer.compare(v1.getId(), v2.getId());
        }).orElse(null);
    }
    public boolean assignerReservationAutomatiquement(int reservationId, LocalDateTime ancreVague) {
        ReservationService resService = new ReservationService();
        Reservation res = resService.getReservationById(reservationId);
        if (res == null) return false;

        // 1) Tentative classique (un seul véhicule)
        int missionId = chercherMissionCompatible(res);
        if (missionId != -1) {
            if (tenterAjoutDansMission(missionId, res)) {
                synchroniserDepartEtRetoursVague(getHeureArriveeAeroMission(missionId));
                return true; 
            }
        }

        // 2) Nouvelle mission (un seul véhicule neuf)
        if (creerNouvelleMission(res)) {
            synchroniserDepartEtRetoursVague(determinerAncreVague(res.getDateHeure()));
            return true;
        }

        // 3) NOUVEAU : Répartition sur plusieurs véhicules
        System.out.println("Capacité insuffisante pour un seul véhicule. Tentative de répartition pour Res ID: " + res.getId());
        return tenterRepartitionPassagers(res, ancreVague);
    }
    private boolean tenterRepartitionPassagers(Reservation res, LocalDateTime ancreVague) {
        int passagersRestants = res.getNbPassager();
        int minutesAttente = Integer.parseInt(getParametre("TEMPS_ATTENTE_MIN", "15"));
        LocalDateTime finVague = ancreVague.plusMinutes(minutesAttente);

        // 1. On remplit d'abord les trous dans les missions existantes
        List<MissionCapacite> missions = getMissionsOuvertesDeLaVague(ancreVague, finVague);
        for (MissionCapacite mc : missions) {
            if (passagersRestants <= 0) break;
            int aPrendre = Math.min(passagersRestants, mc.placesLibres);
            enregistrerAssignationPartielle(mc.id, res.getId(), aPrendre);
            passagersRestants -= aPrendre;
        }

        // 2. Tant qu'il reste du monde, on crée de nouvelles missions
        while (passagersRestants > 0) {
            Vehicule v = chercherMeilleurVehiculePourSplit(ancreVague, finVague, passagersRestants);
            if (v == null) break; // Plus aucun véhicule de libre !

            int mId = creerMissionVide(v.getId(), ancreVague);
            if (mId != -1) {
                int aPrendre = Math.min(passagersRestants, v.getNbPlaces());
                enregistrerAssignationPartielle(mId, res.getId(), aPrendre);
                passagersRestants -= aPrendre;
            } else {
                break; 
            }
        }

        // 3. S'il reste encore du monde (flotte saturée), on crée le reliquat
        if (passagersRestants > 0 && passagersRestants < res.getNbPassager()) {
            dupliquerReservationPourReliquat(res, passagersRestants);
        }

        synchroniserDepartEtRetoursVague(ancreVague);
        return true;
    }
    private Vehicule chercherMeilleurVehiculePourSplit(LocalDateTime ancre, LocalDateTime fin, int passagersRestants) {
        List<Vehicule> candidats = new ArrayList<>();
        String sql = "SELECT v.*, tc.libelle as carb FROM Vehicule v " +
                    "JOIN TypeCarburant tc ON v.typeCarburant_id = tc.id " +
                    "WHERE v.id NOT IN (SELECT id_vehicule FROM Mission WHERE heure_arrivee_aero = ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(ancre));
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                int vId = rs.getInt("id");
                LocalDateTime dispo = getHeureDisponibiliteVehiculePourVague(vId, ancre, fin);
                if (dispo != null && !dispo.isAfter(fin)) {
                    Vehicule v = new Vehicule();
                    v.setId(vId);
                    v.setNbPlaces(rs.getInt("nbPlaces"));
                    v.setTypeCarburantLibelle(rs.getString("carb"));
                    Time heureDebutDispo = rs.getTime("heure_debut_disponibilite");
                    v.setHeureDebutDisponibilite(heureDebutDispo != null ? heureDebutDispo.toLocalTime() : LocalTime.MIDNIGHT);
                    candidats.add(v);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        if (candidats.isEmpty()) return null;

        // --- LOGIQUE DE TRI "PLUS PROCHE OU PLUS GRAND" ---
        return candidats.stream().min((v1, v2) -> {
            boolean v1AssezGrand = v1.getNbPlaces() >= passagersRestants;
            boolean v2AssezGrand = v2.getNbPlaces() >= passagersRestants;

            // CAS 1 : Les deux peuvent contenir tout le monde -> On prend le plus petit des deux
            if (v1AssezGrand && v2AssezGrand) {
                return Integer.compare(v1.getNbPlaces(), v2.getNbPlaces());
            }
            // CAS 2 : Un seul peut contenir tout le monde -> Celui-là est prioritaire
            if (v1AssezGrand) return -1;
            if (v2AssezGrand) return 1;

            // CAS 3 : Aucun ne peut contenir tout le monde -> On prend le PLUS GRAND disponible
            int compTaille = Integer.compare(v2.getNbPlaces(), v1.getNbPlaces());
            if (compTaille != 0) return compTaille;

            // --- CRITÈRES SECONDAIRES (Égalité de taille) ---
            int m1 = getNombreTrajetsVehiculePourJour(v1.getId(), ancre);
            int m2 = getNombreTrajetsVehiculePourJour(v2.getId(), ancre);
            if (m1 != m2) return Integer.compare(m1, m2);

            if (v1.getTypeCarburantLibelle().equalsIgnoreCase("Diesel") && !v2.getTypeCarburantLibelle().equalsIgnoreCase("Diesel")) return -1;
            if (!v1.getTypeCarburantLibelle().equalsIgnoreCase("Diesel") && v2.getTypeCarburantLibelle().equalsIgnoreCase("Diesel")) return 1;

            return Integer.compare(v1.getId(), v2.getId());
        }).orElse(null);
    }
    private List<MissionCapacite> getMissionsOuvertesDeLaVague(LocalDateTime ancre, LocalDateTime fin) {
        List<MissionCapacite> list = new ArrayList<>();
        String sql = "SELECT m.id, (v.nbPlaces - COALESCE(SUM(vr.nb_passagers_pris), 0)) as libres " +
                    "FROM Mission m " +
                    "JOIN Vehicule v ON m.id_vehicule = v.id " +
                    "LEFT JOIN Vehicules_Reservations vr ON m.id = vr.id_mission " +
                    "WHERE m.heure_arrivee_aero = ? " +
                    "GROUP BY m.id, v.nbPlaces " +
                    "HAVING (v.nbPlaces - COALESCE(SUM(vr.nb_passagers_pris), 0)) > 0";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(ancre));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new MissionCapacite(rs.getInt("id"), rs.getInt("libres")));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
    private int creerMissionVide(int vehiculeId, LocalDateTime ancre) {
        String sql = "INSERT INTO Mission (id_vehicule, heure_arrivee_aero, heure_depart_prevu, heure_retour_prevu) " +
                    "VALUES (?, ?, ?, ?) RETURNING id";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, vehiculeId);
            pstmt.setTimestamp(2, Timestamp.valueOf(ancre));
            pstmt.setTimestamp(3, Timestamp.valueOf(ancre)); // Sera mis à jour par synchroniser...
            pstmt.setTimestamp(4, Timestamp.valueOf(ancre.plusMinutes(30))); 
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return -1;
    }
    private void enregistrerAssignationPartielle(int mId, int rId, int nb) {
        String sql = "INSERT INTO Vehicules_Reservations (id_mission, id_reservation, nb_passagers_pris) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, mId);
            pstmt.setInt(2, rId);
            pstmt.setInt(3, nb);
            pstmt.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }
    private void dupliquerReservationPourReliquat(Reservation res, int reste) {
        String sql = "INSERT INTO Reservation (id_lieu, client, nbPassager, dateHeure) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, res.getIdLieu());
            pstmt.setString(2, res.getClient() + " (Reliquat)");
            pstmt.setInt(3, reste);
            pstmt.setTimestamp(4, Timestamp.valueOf(res.getDateHeure()));
            pstmt.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
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

            enregistrerAssignation(missionId, nouvelleRes.getId(), nouvelleRes.getNbPassager());
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
                Time heureDebutDispo = rs.getTime("heure_debut_disponibilite");
                v.setHeureDebutDisponibilite(heureDebutDispo != null ? heureDebutDispo.toLocalTime() : LocalTime.MIDNIGHT);

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
                .comparing((Vehicule v) -> v.getNbPlaces() >= capaciteCible ? 0 : 1)
                .thenComparingInt(v -> Math.abs(v.getNbPlaces() - res.getNbPassager()))
                .thenComparingInt((Vehicule v) -> trajetsParVehicule.getOrDefault(v.getId(), 0))
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
                    enregistrerAssignation(rs.getInt(1), res.getId(), res.getNbPassager());
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
        
        // On cherche dans notre liste d'ancres calculées au début du traitement
        for (LocalDateTime ancre : this.ancres) {
            LocalDateTime finVague = ancre.plusMinutes(minutesAttente);
            
            // Si la réservation tombe dans cette fenêtre d'ancre
            if (!dateHeureReservation.isBefore(ancre) && !dateHeureReservation.isAfter(finVague)) {
                return ancre;
            }
        }
        
        // Si vraiment rien n'est trouvé (ne devrait pas arriver avec ton pré-traitement)
        return dateHeureReservation;
    }

    private List<LocalDateTime> getAncresVaguesCompatibles(LocalDateTime dateHeureReservation) {
        List<LocalDateTime> ancresCompatibles = new ArrayList<>();
        int minutesAttente = Integer.parseInt(getParametre("TEMPS_ATTENTE_MIN", "15"));

        // On parcourt la liste des ancres déjà créées pour le traitement en cours
        for (LocalDateTime ancre : this.ancres) {
            LocalDateTime finVague = ancre.plusMinutes(minutesAttente);

            // Une réservation est compatible si :
            // 1. Elle n'arrive pas AVANT l'ancre (car l'ancre est le premier arrivé)
            // 2. Elle n'arrive pas APRÈS la fin de la fenêtre d'attente
            if (!dateHeureReservation.isBefore(ancre) && !dateHeureReservation.isAfter(finVague)) {
                ancresCompatibles.add(ancre);
            }
        }

        // Optionnel : Trier pour proposer l'ancre la plus proche en premier
        ancresCompatibles.sort(Comparator.naturalOrder());
        
        return ancresCompatibles;
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
                Time heureDebutDispo = rs.getTime("heure_debut_disponibilite");
                v.setHeureDebutDisponibilite(heureDebutDispo != null ? heureDebutDispo.toLocalTime() : LocalTime.MIDNIGHT);

                LocalDateTime debutAutorise = getHeureDebutDisponibiliteVehiculePourJour(v.getId(), t.toLocalDate());
                if (debutAutorise == null || !t.isBefore(debutAutorise)) {
                    list.add(v);
                }
            }
        } catch (Exception e) {}
        return list;
    }

    private LocalDateTime getHeureDisponibiliteVehiculePourVague(int vehiculeId, LocalDateTime debutVague, LocalDateTime finVague) {
        return getHeureDisponibiliteVehiculePourVague(vehiculeId, debutVague, finVague, -1);
    }

    private LocalDateTime getHeureDisponibiliteVehiculePourVague(int vehiculeId, LocalDateTime debutVague, LocalDateTime finVague, int missionIdExclue) {
        LocalDateTime debutDisponibiliteJour = getHeureDebutDisponibiliteVehiculePourJour(vehiculeId, debutVague.toLocalDate());

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
                LocalDateTime retourMission = rs.getTimestamp(1).toLocalDateTime();
                if (debutDisponibiliteJour != null && retourMission.isBefore(debutDisponibiliteJour)) {
                    return debutDisponibiliteJour;
                }
                return retourMission;
            }
        } catch (Exception e) {}

        // Sinon, on prend son dernier retour avant la fin de vague (si existant),
        // ou le début de vague s'il n'a pas encore roulé ce jour-là.
        LocalDateTime retourAvantFin = getHeureArriveeEffective(vehiculeId, finVague, missionIdExclue);
        LocalDateTime baseDisponibilite = retourAvantFin;
        if (baseDisponibilite == null || baseDisponibilite.isBefore(debutVague)) {
            baseDisponibilite = debutVague;
        }

        if (debutDisponibiliteJour != null && baseDisponibilite.isBefore(debutDisponibiliteJour)) {
            baseDisponibilite = debutDisponibiliteJour;
        }
        return baseDisponibilite;
    }

    private LocalDateTime getHeureDebutDisponibiliteVehiculePourJour(int vehiculeId, LocalDate jourReference) {
        String sql = "SELECT heure_debut_disponibilite FROM Vehicule WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, vehiculeId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getTime(1) != null) {
                return LocalDateTime.of(jourReference, rs.getTime(1).toLocalTime());
            }
        } catch (Exception e) {}
        return LocalDateTime.of(jourReference, LocalTime.MIDNIGHT);
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

    private void enregistrerAssignation(int mId, int rId, int nbPassagersPris) {
        String sql = "INSERT INTO Vehicules_Reservations (id_mission, id_reservation, nb_passagers_pris) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, mId); pstmt.setInt(2, rId); pstmt.setInt(3, nbPassagersPris);
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