package com.entity;

import java.util.Objects;

public class Vehicule {
    private int id;
    private String reference;
    private int nbPlaces;
    private int typeCarburantId;
    private String typeCarburantLibelle; // Pour l'affichage
    
    // Constructeurs
    public Vehicule() {}
    
    public Vehicule(String reference, int nbPlaces, int typeCarburantId) {
        this.reference = reference;
        this.nbPlaces = nbPlaces;
        this.typeCarburantId = typeCarburantId;
    }
    
    public Vehicule(int id, String reference, int nbPlaces, int typeCarburantId) {
        this.id = id;
        this.reference = reference;
        this.nbPlaces = nbPlaces;
        this.typeCarburantId = typeCarburantId;
    }
    
    // Getters et Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getReference() {
        return reference;
    }
    
    public void setReference(String reference) {
        this.reference = reference;
    }
    
    public int getNbPlaces() {
        return nbPlaces;
    }
    
    public void setNbPlaces(int nbPlaces) {
        this.nbPlaces = nbPlaces;
    }
    
    public int getTypeCarburantId() {
        return typeCarburantId;
    }
    
    public void setTypeCarburantId(int typeCarburantId) {
        this.typeCarburantId = typeCarburantId;
    }
    
    public String getTypeCarburantLibelle() {
        return typeCarburantLibelle;
    }
    
    public void setTypeCarburantLibelle(String typeCarburantLibelle) {
        this.typeCarburantLibelle = typeCarburantLibelle;
    }
    
    // Méthodes utilitaires
    @Override
    public String toString() {
        return "Vehicule{" +
                "id=" + id +
                ", reference='" + reference + '\'' +
                ", nbPlaces=" + nbPlaces +
                ", typeCarburantId=" + typeCarburantId +
                ", typeCarburantLibelle='" + typeCarburantLibelle + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vehicule vehicule = (Vehicule) o;
        return id == vehicule.id && 
               nbPlaces == vehicule.nbPlaces && 
               typeCarburantId == vehicule.typeCarburantId && 
               Objects.equals(reference, vehicule.reference);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, reference, nbPlaces, typeCarburantId);
    }
}