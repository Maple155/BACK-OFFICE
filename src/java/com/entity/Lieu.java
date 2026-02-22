package com.entity;

import java.util.Objects;

public class Lieu {
    private int id;
    private String code;
    private String libelle;

    public Lieu() {}

    public Lieu(String code, String libelle) {
        this.code = code;
        this.libelle = libelle;
    }

    public Lieu(int id, String code, String libelle) {
        this.id = id;
        this.code = code;
        this.libelle = libelle;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    @Override
    public String toString() {
        return "Lieu{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", libelle='" + libelle + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Lieu lieu = (Lieu) o;
        return id == lieu.id && Objects.equals(code, lieu.code) && Objects.equals(libelle, lieu.libelle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code, libelle);
    }
}