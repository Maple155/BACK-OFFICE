package com.service;

import com.entity.Reservation;
import com.entity.Lieu;
import com.connect.DatabaseConnection;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReservationService {
    
    public boolean insertReservation(Reservation reservation) {
        String sql = "INSERT INTO Reservation (id_lieu, client, nbPassager, dateHeure) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, reservation.getIdLieu());
            pstmt.setString(2, reservation.getClient());
            pstmt.setInt(3, reservation.getNbPassager());
            pstmt.setTimestamp(4, Timestamp.valueOf(reservation.getDateHeure()));
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'insertion de la réservation: " + e.getMessage());
            return false;
        }
    }
        
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
            System.err.println("Erreur lors de la récupération des lieux: " + e.getMessage());
        }
        
        return lieux;
    }
    
    // Méthode pour supprimer une réservation
    public boolean deleteReservation(int id) {
        String sql = "DELETE FROM Reservation WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression de la réservation: " + e.getMessage());
            return false;
        }
    }

    public String getLieuCodeById(int lieuId) {
        String sql = "SELECT code FROM Lieu WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, lieuId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("code");
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du code du lieu: " + e.getMessage());
        }
        
        return "Lieu inconnu";
    }
    
    public List<Reservation> getAllReservations() {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT r.*, l.code as lieu_code FROM Reservation r " +
                    "JOIN Lieu l ON r.id_lieu = l.id " +
                    "ORDER BY r.dateHeure DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Reservation reservation = new Reservation();
                reservation.setId(rs.getInt("id"));
                reservation.setIdLieu(rs.getInt("id_lieu"));
                reservation.setClient(rs.getString("client"));
                reservation.setNbPassager(rs.getInt("nbPassager"));
                
                Timestamp timestamp = rs.getTimestamp("dateHeure");
                if (timestamp != null) {
                    reservation.setDateHeure(timestamp.toLocalDateTime());
                }
                
                String lieuCode = rs.getString("lieu_code");
                reservation.setLieuCode(lieuCode != null ? lieuCode : getLieuCodeById(rs.getInt("id_lieu")));
                
                reservations.add(reservation);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des réservations: " + e.getMessage());
            
            // Fallback: essayer sans JOIN
            try {
                return getAllReservationsFallback();
            } catch (SQLException e2) {
                System.err.println("Erreur avec fallback: " + e2.getMessage());
            }
        }
        
        return reservations;
    }
    
    public List<Reservation> getAllReservationsFallback() throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT * FROM Reservation ORDER BY dateHeure DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Reservation reservation = new Reservation();
                reservation.setId(rs.getInt("id"));
                reservation.setIdLieu(rs.getInt("id_lieu"));
                reservation.setClient(rs.getString("client"));
                reservation.setNbPassager(rs.getInt("nbPassager"));
                
                Timestamp timestamp = rs.getTimestamp("dateHeure");
                if (timestamp != null) {
                    reservation.setDateHeure(timestamp.toLocalDateTime());
                }
                
                reservation.setLieuCode(getLieuCodeById(rs.getInt("id_lieu")));
                
                reservations.add(reservation);
            }
        }
        
        return reservations;
    }

    public Reservation getReservationById(int id) {
        String sql = "SELECT r.*, l.code as lieu_code FROM Reservation r " +
                    "LEFT JOIN Lieu l ON r.id_lieu = l.id " +
                    "WHERE r.id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Reservation reservation = new Reservation();
                reservation.setId(rs.getInt("id"));
                reservation.setIdLieu(rs.getInt("id_lieu"));
                reservation.setClient(rs.getString("client"));
                reservation.setNbPassager(rs.getInt("nbPassager"));
                
                Timestamp timestamp = rs.getTimestamp("dateHeure");
                if (timestamp != null) {
                    reservation.setDateHeure(timestamp.toLocalDateTime());
                }
                
                String lieuCode = rs.getString("lieu_code");
                reservation.setLieuCode(lieuCode != null ? lieuCode : getLieuCodeById(rs.getInt("id_lieu")));
                
                return reservation;
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la réservation par ID: " + e.getMessage());
        }
        
        return null;
    }

    public List<Map<String, Object>> getAssignedReservationsByDate(LocalDate date) {
        return getAssignedReservationsByDateRange(date, date);
    }

    public List<Map<String, Object>> getAssignedReservationsByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> rows = new ArrayList<>();
        String sql = "SELECT v.id as vehicule_id, v.marque || ' ' || v.modele as vehicule, " +
                "r.id, r.client, r.nbPassager, r.dateHeure, l.code as lieu_code " +
                "FROM Vehicules_Reservations vr " +
                "JOIN Vehicule v ON vr.id_voiture = v.id " +
                "JOIN Reservation r ON vr.id_reservation = r.id " +
                "JOIN Lieu l ON r.id_lieu = l.id " +
                "WHERE DATE(r.dateHeure) >= ? AND DATE(r.dateHeure) <= ? " +
                "ORDER BY r.dateHeure, v.marque";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("vehicule", rs.getString("vehicule"));
                row.put("client", rs.getString("client"));
                row.put("nbPassager", rs.getInt("nbPassager"));
                row.put("lieuCode", rs.getString("lieu_code"));

                Timestamp timestamp = rs.getTimestamp("dateHeure");
                if (timestamp != null) {
                    row.put("dateHeure", timestamp.toLocalDateTime().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                }

                rows.add(row);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des réservations assignées: " + e.getMessage());
        }

        return rows;
    }

    public List<Reservation> getUnassignedReservationsByDate(LocalDate date) {
        return getUnassignedReservationsByDateRange(date, date);
    }

    public List<Reservation> getUnassignedReservationsByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT r.*, l.code as lieu_code " +
                "FROM Reservation r " +
                "JOIN Lieu l ON r.id_lieu = l.id " +
                "LEFT JOIN Vehicules_Reservations vr ON vr.id_reservation = r.id " +
                "WHERE DATE(r.dateHeure) >= ? AND DATE(r.dateHeure) <= ? AND vr.id IS NULL " +
                "ORDER BY r.dateHeure";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(startDate));
            pstmt.setDate(2, Date.valueOf(endDate));
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Reservation reservation = new Reservation();
                reservation.setId(rs.getInt("id"));
                reservation.setIdLieu(rs.getInt("id_lieu"));
                reservation.setClient(rs.getString("client"));
                reservation.setNbPassager(rs.getInt("nbPassager"));
                reservation.setLieuCode(rs.getString("lieu_code"));

                Timestamp timestamp = rs.getTimestamp("dateHeure");
                if (timestamp != null) {
                    reservation.setDateHeure(timestamp.toLocalDateTime());
                }

                reservations.add(reservation);
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des réservations non assignées: " + e.getMessage());
        }

        return reservations;
    }
}