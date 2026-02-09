package com.connect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/locations";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";
    
    public static Connection getConnection() throws SQLException {
        try {
            // Charger le driver PostgreSQL
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver PostgreSQL non trouvé", e);
        }
        
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    
    public static void testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                System.out.println("Connexion à PostgreSQL établie avec succès !");
            }
        } catch (SQLException e) {
            System.out.println("Erreur de connexion : " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        testConnection();
    }
}