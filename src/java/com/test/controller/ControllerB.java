package com.test.controller;

import annotation.Controller;
import annotation.Get;
import annotation.GetURL;
import annotation.Param;
import annotation.RequestMapping;
import service.ModelView;

@Controller
public class ControllerB {
    
    @Get
    @GetURL(url = "/testController")
    public String testController() {
        return "Fonction test controller";
    }

    @RequestMapping
    @GetURL(url = "/testFormulaire")
    public String testFormulaire(@Param("nom") Object var1, @Param("age") Object var2) {
        ModelView session = new ModelView();
        session.addSession("test", "Bonjour");
        return "test du formulaire var1 : " + var1.toString() + " et var2 : " + Integer.parseInt(var2.toString());
    }

    // Version sans annotation - les noms de paramètres doivent correspondre aux noms du formulaire
    @RequestMapping
    @GetURL(url = "/testFormulaireSansParam")
    public String testFormulaireSansParam(Object nom, Object age) {
        return "test du formulaire sans @Param - nom : " + nom.toString() + " et age : " + Integer.parseInt(age.toString());
    }
}