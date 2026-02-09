package com.service;

import com.entity.Token;
import com.connect.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TokenService {
    
    public List<Token> getAllTokens() {
        List<Token> tokens = new ArrayList<>();
        String sql = "SELECT * FROM Tokens ORDER BY dateExpiration DESC";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Token token = new Token();
                token.setId(rs.getInt("id"));
                token.setToken(UUID.fromString(rs.getString("token")));
                token.setDateExpiration(rs.getTimestamp("dateExpiration").toLocalDateTime());
                tokens.add(token);
            }
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des tokens: " + e.getMessage());
        }
        
        return tokens;
    }
    
    public boolean insertToken(Token token) {
        String sql = "INSERT INTO Tokens (token, dateExpiration) VALUES (?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, token.getToken().toString());
            pstmt.setTimestamp(2, Timestamp.valueOf(token.getDateExpiration()));
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
            
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'insertion du token: " + e.getMessage());
            return false;
        }
    }

    public boolean checkToken(UUID token) {
        List<Token> tokens = getAllTokens();

        for (Token token2 : tokens) {
            if (token2.getToken().equals(token) && !token2.isExpired()) {
                return true;
            }
        }

        return false;
    }

}
