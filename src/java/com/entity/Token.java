package com.entity;

import java.time.LocalDateTime;
import java.util.UUID;

public class Token {
    private int id;
    private UUID token;
    private LocalDateTime dateExpiration;
    
    // Constructeurs
    public Token() {}
    
    public Token(UUID token, LocalDateTime dateExpiration) {
        this.token = token;
        this.dateExpiration = dateExpiration;
    }
    
    public Token(int id, UUID token, LocalDateTime dateExpiration) {
        this.id = id;
        this.token = token;
        this.dateExpiration = dateExpiration;
    }
    
    // Getters et Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public UUID getToken() {
        return token;
    }
    
    public void setToken(UUID token) {
        this.token = token;
    }
    
    public LocalDateTime getDateExpiration() {
        return dateExpiration;
    }
    
    public void setDateExpiration(LocalDateTime dateExpiration) {
        this.dateExpiration = dateExpiration;
    }
    
    // Méthodes utilitaires
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(dateExpiration);
    }
    
    @Override
    public String toString() {
        return "Token{" +
                "id=" + id +
                ", token=" + token +
                ", dateExpiration=" + dateExpiration +
                ", isExpired=" + isExpired() +
                '}';
    }
}