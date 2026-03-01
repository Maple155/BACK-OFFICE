package com.connect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static String env(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    private static String getJdbcUrl() {
        String host = env("PGHOST", "localhost");
        String port = env("PGPORT", "5432");
        String database = env("PGDATABASE", "locations");
        boolean isLocalHost = "localhost".equalsIgnoreCase(host)
                || "127.0.0.1".equals(host)
                || "host.docker.internal".equalsIgnoreCase(host);
        String defaultSslMode = isLocalHost ? "disable" : "require";
        String sslMode = env("PGSSLMODE", defaultSslMode);

        return "jdbc:postgresql://" + host + ":" + port + "/" + database + "?sslmode=" + sslMode;
    }

    private static String getUser() {
        return env("PGUSER", "postgres");
    }

    private static String getPassword() {
        return env("PGPASSWORD", "root");
    }
    
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver PostgreSQL non trouvé", e);
        }

        return DriverManager.getConnection(getJdbcUrl(), getUser(), getPassword());
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