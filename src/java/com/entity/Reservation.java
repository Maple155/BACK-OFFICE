package com.entity;

import java.time.LocalDateTime;
import java.util.Objects;

public class Reservation {
    private int id;
    private int idLieu;
    private String client;
    private int nbPassager;
    private LocalDateTime dateHeure;
    private String lieuCode;

    // Constructeurs
    public Reservation() {}
    
    public Reservation(int idLieu, String client, int nbPassager, LocalDateTime dateHeure) {
        this.idLieu = idLieu;
        this.client = client;
        this.nbPassager = nbPassager;
        this.dateHeure = dateHeure;
    }
    
    public Reservation(int id, int idLieu, String client, int nbPassager, LocalDateTime dateHeure) {
        this.id = id;
        this.idLieu = idLieu;
        this.client = client;
        this.nbPassager = nbPassager;
        this.dateHeure = dateHeure;
    }
    
    // Getters et Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getIdLieu() {
        return idLieu;
    }
    
    public void setIdLieu(int idLieu) {
        this.idLieu = idLieu;
    }
    
    public String getClient() {
        return client;
    }
    
    public void setClient(String client) {
        this.client = client;
    }
    
    public int getNbPassager() {
        return nbPassager;
    }
    
    public void setNbPassager(int nbPassager) {
        this.nbPassager = nbPassager;
    }
    
    public LocalDateTime getDateHeure() {
        return dateHeure;
    }
    
    public void setDateHeure(LocalDateTime dateHeure) {
        this.dateHeure = dateHeure;
    }

    public String getLieuCode() {
        return lieuCode;
    }

    public void setLieuCode(String lieuCode) {
        this.lieuCode = lieuCode;
    }
    
    // Méthodes utilitaires
    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
            ", idLieu=" + idLieu +
                ", client='" + client + '\'' +
                ", nbPassager=" + nbPassager +
                ", dateHeure=" + dateHeure +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return id == that.id && 
             idLieu == that.idLieu && 
               nbPassager == that.nbPassager && 
               Objects.equals(client, that.client) && 
               Objects.equals(dateHeure, that.dateHeure);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, idLieu, client, nbPassager, dateHeure);
    }
}