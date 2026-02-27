package com.entity;

import java.util.Objects;

public class Hotel {
    private int id;
    private String nom;
    
    // Constructeurs
    public Hotel() {}
     
    public Hotel(String nom) {
        this.nom = nom;
    }
    
    public Hotel(int id, String nom) {
        this.id = id;
        this.nom = nom;
    }
    
    // Getters et Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getNom() {
        return nom;
    }
    
    public void setNom(String nom) {
        this.nom = nom;
    }
    
    // Méthodes utilitaires
    @Override
    public String toString() {
        return "Hotel{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Hotel hotel = (Hotel) o;
        return id == hotel.id && Objects.equals(nom, hotel.nom);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, nom);
    }
}