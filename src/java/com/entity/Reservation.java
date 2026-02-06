package com.entity;

import java.time.LocalDateTime;
import java.util.Objects;

public class Reservation {
    private int id;
    private int idHotel;
    private String client;
    private int nbPassager;
    private LocalDateTime dateHeure;
    private String hotelNom;

    // Constructeurs
    public Reservation() {}
    
    public Reservation(int idHotel, String client, int nbPassager, LocalDateTime dateHeure) {
        this.idHotel = idHotel;
        this.client = client;
        this.nbPassager = nbPassager;
        this.dateHeure = dateHeure;
    }
    
    public Reservation(int id, int idHotel, String client, int nbPassager, LocalDateTime dateHeure) {
        this.id = id;
        this.idHotel = idHotel;
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
    
    public int getIdHotel() {
        return idHotel;
    }
    
    public void setIdHotel(int idHotel) {
        this.idHotel = idHotel;
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

    public String getHotelNom() {
        return hotelNom;
    }

    public void setHotelNom(String hotelNom) {
        this.hotelNom = hotelNom;
    }
    
    // Méthodes utilitaires
    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", idHotel=" + idHotel +
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
               idHotel == that.idHotel && 
               nbPassager == that.nbPassager && 
               Objects.equals(client, that.client) && 
               Objects.equals(dateHeure, that.dateHeure);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, idHotel, client, nbPassager, dateHeure);
    }
}