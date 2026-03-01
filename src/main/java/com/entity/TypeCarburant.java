package com.entity;

import java.util.Objects;

public class TypeCarburant {
    private int id;
    private String libelle;
    
    // Constructeurs
    public TypeCarburant() {}
    
    public TypeCarburant(String libelle) {
        this.libelle = libelle;
    }
    
    public TypeCarburant(int id, String libelle) {
        this.id = id;
        this.libelle = libelle;
    }
    
    // Getters et Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getLibelle() {
        return libelle;
    }
    
    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }
    
    // Méthodes utilitaires
    @Override
    public String toString() {
        return "TypeCarburant{" +
                "id=" + id +
                ", libelle='" + libelle + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypeCarburant that = (TypeCarburant) o;
        return id == that.id && Objects.equals(libelle, that.libelle);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, libelle);
    }
}