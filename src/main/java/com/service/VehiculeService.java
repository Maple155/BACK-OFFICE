package com.service;

import com.entity.Vehicule;
import com.entity.TypeCarburant;
import com.connect.DatabaseConnection;
import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class VehiculeService {
    
    // Méthodes pour TypeCarburant
    
    public List<TypeCarburant> getAllTypesCarburant() {
        List<TypeCarburant> types = new ArrayList<>();
        String sql = "SELECT * FROM TypeCarburant ORDER BY libelle";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                TypeCarburant type = new TypeCarburant();
                type.setId(rs.getInt("id"));
                type.setLibelle(rs.getString("libelle"));
                types.add(type);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des types de carburant: " + e.getMessage());
        }
        
        return types;
    }
    
    public TypeCarburant getTypeCarburantById(int id) {
        String sql = "SELECT * FROM TypeCarburant WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                TypeCarburant type = new TypeCarburant();
                type.setId(rs.getInt("id"));
                type.setLibelle(rs.getString("libelle"));
                return type;
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du type de carburant: " + e.getMessage());
        }
        
        return null;
    }
    
    public boolean insertTypeCarburant(TypeCarburant type) {
        String sql = "INSERT INTO TypeCarburant (libelle) VALUES (?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, type.getLibelle());
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'insertion du type de carburant: " + e.getMessage());
            return false;
        }
    }
    
    // Méthodes pour Vehicule
    
    public boolean insertVehicule(Vehicule vehicule) {
        String sql = "INSERT INTO Vehicule (reference, nbPlaces, heure_debut_disponibilite, typeCarburant_id) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            LocalTime heureDebut = vehicule.getHeureDebutDisponibilite() != null ? vehicule.getHeureDebutDisponibilite() : LocalTime.MIDNIGHT;
            
            pstmt.setString(1, vehicule.getReference());
            pstmt.setInt(2, vehicule.getNbPlaces());
            pstmt.setTime(3, Time.valueOf(heureDebut));
            pstmt.setInt(4, vehicule.getTypeCarburantId());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'insertion du véhicule: " + e.getMessage());
            return false;
        }
    }
    
    public boolean updateVehicule(Vehicule vehicule) {
        String sql = "UPDATE Vehicule SET reference = ?, nbPlaces = ?, heure_debut_disponibilite = ?, typeCarburant_id = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            LocalTime heureDebut = vehicule.getHeureDebutDisponibilite() != null ? vehicule.getHeureDebutDisponibilite() : LocalTime.MIDNIGHT;
            
            pstmt.setString(1, vehicule.getReference());
            pstmt.setInt(2, vehicule.getNbPlaces());
            pstmt.setTime(3, Time.valueOf(heureDebut));
            pstmt.setInt(4, vehicule.getTypeCarburantId());
            pstmt.setInt(5, vehicule.getId());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour du véhicule: " + e.getMessage());
            return false;
        }
    }
    
    public boolean deleteVehicule(int id) {
        String sql = "DELETE FROM Vehicule WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression du véhicule: " + e.getMessage());
            return false;
        }
    }
    
    public List<Vehicule> getAllVehicules() {
        List<Vehicule> vehicules = new ArrayList<>();
        String sql = "SELECT v.*, t.libelle as type_carburant_libelle " +
                    "FROM Vehicule v " +
                    "LEFT JOIN TypeCarburant t ON v.typeCarburant_id = t.id " +
                    "ORDER BY v.reference";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Vehicule vehicule = new Vehicule();
                vehicule.setId(rs.getInt("id"));
                vehicule.setReference(rs.getString("reference"));
                vehicule.setNbPlaces(rs.getInt("nbPlaces"));
                Time heureDebutDispo = rs.getTime("heure_debut_disponibilite");
                vehicule.setHeureDebutDisponibilite(heureDebutDispo != null ? heureDebutDispo.toLocalTime() : LocalTime.MIDNIGHT);
                vehicule.setTypeCarburantId(rs.getInt("typeCarburant_id"));
                vehicule.setTypeCarburantLibelle(rs.getString("type_carburant_libelle"));
                vehicules.add(vehicule);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des véhicules: " + e.getMessage());
        }
        
        return vehicules;
    }
    
    public Vehicule getVehiculeById(int id) {
        String sql = "SELECT v.*, t.libelle as type_carburant_libelle " +
                    "FROM Vehicule v " +
                    "LEFT JOIN TypeCarburant t ON v.typeCarburant_id = t.id " +
                    "WHERE v.id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Vehicule vehicule = new Vehicule();
                vehicule.setId(rs.getInt("id"));
                vehicule.setReference(rs.getString("reference"));
                vehicule.setNbPlaces(rs.getInt("nbPlaces"));
                Time heureDebutDispo = rs.getTime("heure_debut_disponibilite");
                vehicule.setHeureDebutDisponibilite(heureDebutDispo != null ? heureDebutDispo.toLocalTime() : LocalTime.MIDNIGHT);
                vehicule.setTypeCarburantId(rs.getInt("typeCarburant_id"));
                vehicule.setTypeCarburantLibelle(rs.getString("type_carburant_libelle"));
                return vehicule;
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du véhicule: " + e.getMessage());
        }
        
        return null;
    }
    
    public List<Vehicule> searchVehicules(String keyword) {
        List<Vehicule> vehicules = new ArrayList<>();
        String sql = "SELECT v.*, t.libelle as type_carburant_libelle " +
                    "FROM Vehicule v " +
                    "LEFT JOIN TypeCarburant t ON v.typeCarburant_id = t.id " +
                    "WHERE CAST(v.id AS TEXT) LIKE ? " +
                    "OR UPPER(v.reference) LIKE ? " +
                    "OR CAST(v.nbPlaces AS TEXT) LIKE ? " +
                    "OR CAST(v.heure_debut_disponibilite AS TEXT) LIKE ? " +
                    "OR CAST(v.typeCarburant_id AS TEXT) LIKE ? " +
                    "OR UPPER(COALESCE(t.libelle, '')) LIKE ? " +
                    "ORDER BY v.reference";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + keyword + "%";
            String searchPatternUpper = "%" + keyword.toUpperCase() + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPatternUpper);
            pstmt.setString(3, searchPattern);
            pstmt.setString(4, searchPattern);
            pstmt.setString(5, searchPattern);
            pstmt.setString(6, searchPatternUpper);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Vehicule vehicule = new Vehicule();
                vehicule.setId(rs.getInt("id"));
                vehicule.setReference(rs.getString("reference"));
                vehicule.setNbPlaces(rs.getInt("nbPlaces"));
                Time heureDebutDispo = rs.getTime("heure_debut_disponibilite");
                vehicule.setHeureDebutDisponibilite(heureDebutDispo != null ? heureDebutDispo.toLocalTime() : LocalTime.MIDNIGHT);
                vehicule.setTypeCarburantId(rs.getInt("typeCarburant_id"));
                vehicule.setTypeCarburantLibelle(rs.getString("type_carburant_libelle"));
                vehicules.add(vehicule);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la recherche des véhicules: " + e.getMessage());
        }
        
        return vehicules;
    }
}