package com.test.test;

import java.sql.Date;
import java.util.Arrays;
import java.util.List;

public class User {
    private String name;
    private String prenom;
    private int age;
    private Date dateNaissance;
    private String[] hobbies;
    private List<String> diplome; 

    public User() {
    }

    public User(String name, String prenom, int age, Date dateNaissance, String[] hobbies, List<String> diplome) {
        this.name = name;
        this.prenom = prenom;
        this.age = age;
        this.dateNaissance = dateNaissance;
        this.hobbies = hobbies;
        this.diplome = diplome;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Date getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(Date dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String[] getHobbies() {
        return hobbies;
    }

    public void setHobbies(String[] hobbies) {
        this.hobbies = hobbies;
    }

    public List<String> getDiplome() {
        return diplome;
    }

    public void setDiplome(List<String> diplome) {
        this.diplome = diplome;
    }

    @Override
    public String toString() {
        return "User [name=" + name + ", prenom=" + prenom + ", age=" + age + ", dateNaissance=" + dateNaissance
                + ", hobbies=" + Arrays.toString(hobbies) + ", diplome=" + diplome + "]";
    }
   
}