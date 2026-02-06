package com.test.controller;

import annotation.Controller;
import annotation.Get;
import annotation.GetURL;
import annotation.Protected;
import annotation.Role;
import service.ModelView;

import java.util.Arrays;

@Controller
public class AuthController {

    @Get
    @GetURL(url = "/login")
    public ModelView loginPage() {
        ModelView model = new ModelView("login.jsp");
        return model;
    }
    
    @Get
    @GetURL(url = "/logout")
    public ModelView logoutPage() {
        ModelView model = new ModelView("logout.jsp");
        return model;
    }

    @Get
    @GetURL(url = "/admin/dashboard")
    @Protected
    @Role(role = {"ADMIN", "SUPER_ADMIN"})
    public ModelView adminDashboard() {
        ModelView model = new ModelView("admin/dashboard.jsp");
        model.addObject("message", "Bienvenue dans le tableau de bord administrateur");
        return model;
    }
    
    @Get
    @GetURL(url = "/user/profile")
    @Protected
    @Role(role = {"USER", "ADMIN", "SUPER_ADMIN"})
    public ModelView userProfile() {
        ModelView model = new ModelView("user/profile.jsp");
        model.addObject("message", "Bienvenue dans votre profil utilisateur");
        return model;
    }
    
    @Get
    @GetURL(url = "/public/page")
    public ModelView publicPage() {
        ModelView model = new ModelView("public/page.jsp");
        model.addObject("message", "Cette page est accessible à tous");
        return model;
    }
}