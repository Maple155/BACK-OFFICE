package com.test.controller;

import annotation.Controller;
import annotation.Post;
import annotation.GetURL;
import service.ModelView;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

@Controller
public class LoginController {

    @Post
    @GetURL(url = "/auth/login")
    public ModelView handleLogin(ModelView mv) {
        String username = String.valueOf(mv.getSessionObject("username"));
        String password = String.valueOf(mv.getSessionObject("password"));
        String role = String.valueOf(mv.getSessionObject("role"));
        
        // Ici, normalement vous vérifieriez dans une base de données
        // Pour le test, on accepte n'importe quel utilisateur avec un mot de passe "123"
        
        ModelView model = new ModelView();
        
        if (username != null && !username.trim().isEmpty() && 
            password != null && password.equals("123")) {
            
            // Authentification réussie
            List<String> roles = Arrays.asList(role);
            mv.addSession("loggedUser", username);
            mv.addSession("userRoles", roles);
            
            model.setView("index.jsp");
            model.addObject("success", "Connexion réussie!");
        } else {
            // Échec d'authentification
            model.setView("login.jsp");
            model.addObject("error", "Identifiants incorrects");
        }
        
        return model;
    }
    
    @Get
    @GetURL(url = "/auth/logout")
    public ModelView handleLogout(HttpServletRequest request) {
        request.getSession().invalidate();
        ModelView model = new ModelView("login.jsp");
        model.addObject("message", "Vous avez été déconnecté");
        return model;
    }
}