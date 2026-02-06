package com.service;

import com.entity.Reservation;
import com.entity.Hotel;
import com.connect.DatabaseConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationService {
    
    // Méthode pour insérer une réservation
    public boolean insertReservation(Reservation reservation) {
        String sql = "INSERT INTO Reservation (id_hotel, client, nbPassager, dateHeure) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, reservation.getIdHotel());
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
        
    // Méthode pour récupérer tous les hôtels
    public List<Hotel> getAllHotels() {
        List<Hotel> hotels = new ArrayList<>();
        String sql = "SELECT * FROM Hotel ORDER BY nom";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Hotel hotel = new Hotel();
                hotel.setId(rs.getInt("id"));
                hotel.setNom(rs.getString("nom"));
                hotels.add(hotel);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des hôtels: " + e.getMessage());
        }
        
        return hotels;
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

    public String getHotelNameById(int hotelId) {
        String sql = "SELECT nom FROM Hotel WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, hotelId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getString("nom");
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du nom de l'hôtel: " + e.getMessage());
        }
        
        return "Hôtel inconnu";
    }
    
    // Modifiez la méthode getAllReservations pour inclure les noms d'hôtels
    public List<Reservation> getAllReservations() {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT r.*, h.nom as hotel_nom FROM Reservation r " +
                    "JOIN Hotel h ON r.id_hotel = h.id " +
                    "ORDER BY r.dateHeure DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Reservation reservation = new Reservation();
                reservation.setId(rs.getInt("id"));
                reservation.setIdHotel(rs.getInt("id_hotel"));
                reservation.setClient(rs.getString("client"));
                reservation.setNbPassager(rs.getInt("nbPassager"));
                
                // Convertir Timestamp en LocalDateTime
                Timestamp timestamp = rs.getTimestamp("dateHeure");
                if (timestamp != null) {
                    reservation.setDateHeure(timestamp.toLocalDateTime());
                }
                
                // Récupérer le nom de l'hôtel
                String hotelNom = rs.getString("hotel_nom");
                reservation.setHotelNom(hotelNom != null ? hotelNom : getHotelNameById(rs.getInt("id_hotel")));
                
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
    
    // Méthode fallback si le JOIN ne fonctionne pas
    private List<Reservation> getAllReservationsFallback() throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT * FROM Reservation ORDER BY dateHeure DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Reservation reservation = new Reservation();
                reservation.setId(rs.getInt("id"));
                reservation.setIdHotel(rs.getInt("id_hotel"));
                reservation.setClient(rs.getString("client"));
                reservation.setNbPassager(rs.getInt("nbPassager"));
                
                Timestamp timestamp = rs.getTimestamp("dateHeure");
                if (timestamp != null) {
                    reservation.setDateHeure(timestamp.toLocalDateTime());
                }
                
                // Récupérer le nom de l'hôtel séparément
                reservation.setHotelNom(getHotelNameById(rs.getInt("id_hotel")));
                
                reservations.add(reservation);
            }
        }
        
        return reservations;
    }

    public Reservation getReservationById(int id) {
        String sql = "SELECT r.*, h.nom as hotel_nom FROM Reservation r " +
                    "LEFT JOIN Hotel h ON r.id_hotel = h.id " +
                    "WHERE r.id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Reservation reservation = new Reservation();
                reservation.setId(rs.getInt("id"));
                reservation.setIdHotel(rs.getInt("id_hotel"));
                reservation.setClient(rs.getString("client"));
                reservation.setNbPassager(rs.getInt("nbPassager"));
                
                // Convertir Timestamp en LocalDateTime
                Timestamp timestamp = rs.getTimestamp("dateHeure");
                if (timestamp != null) {
                    reservation.setDateHeure(timestamp.toLocalDateTime());
                }
                
                // Récupérer le nom de l'hôtel
                String hotelNom = rs.getString("hotel_nom");
                reservation.setHotelNom(hotelNom != null ? hotelNom : getHotelNameById(rs.getInt("id_hotel")));
                
                return reservation;
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération de la réservation par ID: " + e.getMessage());
        }
        
        return null;
    }
}