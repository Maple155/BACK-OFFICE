package com.service;

import com.entity.Reservation;
import com.entity.Lieu;
import com.connect.DatabaseConnection;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReservationService {

    // --- GESTION DES LIEUX ---

    public List<Lieu> getAllLieux() {
        List<Lieu> lieux = new ArrayList<>();
        String sql = "SELECT * FROM Lieu ORDER BY code";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Lieu lieu = new Lieu();
                lieu.setId(rs.getInt("id"));
                lieu.setCode(rs.getString("code"));
                lieu.setLibelle(rs.getString("libelle"));
                lieux.add(lieu);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lieux;
    }

    public String getLieuCodeById(int lieuId) {
        String sql = "SELECT code FROM Lieu WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, lieuId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("code");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "N/A";
    }

    // --- GESTION DES RÉSERVATIONS (CRUD) ---

    public boolean insertReservation(Reservation reservation) {
        String sql = "INSERT INTO Reservation (id_lieu, client, nbPassager, dateHeure) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, reservation.getIdLieu());
            pstmt.setString(2, reservation.getClient());
            pstmt.setInt(3, reservation.getNbPassager());
            pstmt.setTimestamp(4, Timestamp.valueOf(reservation.getDateHeure()));
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteReservation(int id) {
        String sql = "DELETE FROM Reservation WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Reservation getReservationById(int id) {
        String sql = "SELECT r.*, l.code as lieu_code FROM Reservation r " +
                    "LEFT JOIN Lieu l ON r.id_lieu = l.id WHERE r.id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapResultSetToReservation(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Reservation> getAllReservations() {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT r.*, l.code as lieu_code FROM Reservation r " +
                    "JOIN Lieu l ON r.id_lieu = l.id ORDER BY r.dateHeure DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                reservations.add(mapResultSetToReservation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reservations;
    }

    // --- FILTRAGE ET ASSIGNATIONS (Logique Métier) ---

    /**
     * Récupère les réservations non encore liées à une mission.
     */
    public List<Reservation> getUnassignedReservationsByDateRange(LocalDate startDate, LocalDate endDate) {
        return getUnassignedReservationsByDateTimeRange(startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
    }

    /**
     * Récupère les réservations non assignées dans un intervalle date+heure précis.
     */
    public List<Reservation> getUnassignedReservationsByDateTimeRange(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT r.*, l.code as lieu_code FROM Reservation r " +
                 "JOIN Lieu l ON r.id_lieu = l.id " +
                 "WHERE r.dateHeure BETWEEN ? AND ? " +
                 "AND r.id NOT IN (SELECT id_reservation FROM Vehicules_Reservations) " +
                 "ORDER BY r.dateHeure";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(startDateTime));
            pstmt.setTimestamp(2, Timestamp.valueOf(endDateTime));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                reservations.add(mapResultSetToReservation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reservations;
    }

    /**
     * Récupère les réservations assignées avec calcul dynamique du statut.
     */
    public List<Map<String, Object>> getAssignedReservationsByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> rows = new ArrayList<>();
        String sql =
                "SELECT vr.nb_passagers_pris as nb_pris,v.reference as vehicule, m.heure_depart_prevu, m.heure_retour_prevu, " +
                "r.id, r.client, r.nbPassager, r.dateHeure, l.code as lieu_code " +
                "FROM Reservation r " +
                "JOIN Vehicules_Reservations vr ON r.id = vr.id_reservation " +
                "JOIN Mission m ON vr.id_mission = m.id " +
                "JOIN Vehicule v ON m.id_vehicule = v.id " +
                "JOIN Lieu l ON r.id_lieu = l.id " +
                "WHERE DATE(r.dateHeure) BETWEEN ? AND ? " +
                "ORDER BY v.reference ASC, m.heure_depart_prevu ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));
            ResultSet rs = pstmt.executeQuery();

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            LocalDateTime now = LocalDateTime.now();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("vehicule", rs.getString("vehicule"));
                row.put("client", rs.getString("client"));
                row.put("nbPassager", rs.getInt("nbPassager"));
                row.put("lieuCode", rs.getString("lieu_code"));
                row.put("nbPris", rs.getInt("nb_pris"));
                
                LocalDateTime dateHeure = rs.getTimestamp("dateHeure").toLocalDateTime();
                LocalDateTime depart = rs.getTimestamp("heure_depart_prevu").toLocalDateTime();
                LocalDateTime retour = rs.getTimestamp("heure_retour_prevu").toLocalDateTime();

                row.put("dateHeure", dateHeure.format(dtf));
                row.put("heureDepart", depart.format(dtf));
                row.put("heureRetour", retour.format(dtf));

                // --- LOGIQUE DE STATUT CALCULÉE ---
                // Le statut n'existe plus en base, on le déduit du temps
                String statusLabel;
                String statusClass; // Pour faciliter l'affichage CSS en JSP

                if (now.isBefore(depart)) {
                    statusLabel = "EN ATTENTE";
                    statusClass = "badge-info";
                } else if (now.isAfter(retour)) {
                    statusLabel = "TERMINÉ";
                    statusClass = "badge-success";
                } else {
                    statusLabel = "EN ROUTE";
                    statusClass = "badge-warning";
                }

                row.put("statutCalcule", statusLabel);
                row.put("statusClass", statusClass);

                rows.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }

    // --- HELPERS ---

    private Reservation mapResultSetToReservation(ResultSet rs) throws SQLException {
        Reservation res = new Reservation();
        res.setId(rs.getInt("id"));
        res.setIdLieu(rs.getInt("id_lieu"));
        res.setClient(rs.getString("client"));
        res.setNbPassager(rs.getInt("nbPassager"));
        res.setLieuCode(rs.getString("lieu_code"));
        Timestamp ts = rs.getTimestamp("dateHeure");
        if (ts != null) {
            res.setDateHeure(ts.toLocalDateTime());
        }
        return res;
    }
    /**
     * Récupère TOUTES les réservations non assignées qui auraient dû être traitées 
     * avant ou pendant la fenêtre de temps indiquée.
     * (Inclut les reliquats créés en base et les oubliés des vagues précédentes)
     */
    public List<Reservation> getUnassignedBeforeOrAt(LocalDateTime limitDateTime) {
        List<Reservation> reservations = new ArrayList<>();
        // On sélectionne les résas qui n'ont AUCUNE entrée dans Vehicules_Reservations
        // et dont l'heure de rendez-vous est passée ou égale à la limite de la vague.
        String sql = "SELECT r.*, l.code as lieu_code FROM Reservation r " +
                    "JOIN Lieu l ON r.id_lieu = l.id " +
                    "WHERE r.id NOT IN (SELECT id_reservation FROM Vehicules_Reservations) " +
                    "AND r.dateHeure <= ? " +
                    "ORDER BY r.dateHeure ASC"; // Chronologique pour traiter les plus vieux d'abord

        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, Timestamp.valueOf(limitDateTime));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                reservations.add(mapResultSetToReservation(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reservations;
    }
}