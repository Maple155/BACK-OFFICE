package com.test.controller;

import java.util.Map;
import annotation.Controller;
import annotation.Get;
import annotation.GetURL;
import annotation.Post;
import annotation.Session;
import service.ModelView;

@Controller
public class SessionExampleController {

    @Get
    @GetURL(url = "/session/read")
    public String readSession(@Session Map<String, Object> session) {
        StringBuilder result = new StringBuilder();
        result.append("<h2>Contenu de la session :</h2>");
        
        if (session.isEmpty()) {
            result.append("<p>La session est vide</p>");
        } else {
            result.append("<ul>");
            for (Map.Entry<String, Object> entry : session.entrySet()) {
                result.append("<li><strong>")
                      .append(entry.getKey())
                      .append(":</strong> ")
                      .append(entry.getValue())
                      .append("</li>");
            }
            result.append("</ul>");
        }
        
        return result.toString();
    }

    @Post
    @GetURL(url = "/session/update")
    public ModelView updateSession(@Session Map<String, Object> session, 
                                   String username, 
                                   String email) {
        // Modifier la Map de session²
        session.put("username", username);
        session.put("email", email);
        session.put("lastUpdate", System.currentTimeMillis());
        
        // Retourner un ModelView avec la session modifiée
        ModelView model = new ModelView("session-updated.jsp");
        model.setSession(session);  // Important : mettre la session dans ModelView
        model.addObject("message", "Session mise à jour avec succès");
        
        return model;
    }
    
    /**
     * Suppression d'éléments de la session
     */
    @Post
    @GetURL(url = "/session/remove")
    public ModelView removeFromSession(@Session Map<String, Object> session, 
                                       String key) {
        session.remove(key);
        
        ModelView model = new ModelView("session-updated.jsp");
        model.setSession(session);
        model.addObject("message", "Clé '" + key + "' supprimée de la session");
        
        return model;
    }

    // ========== MÉTHODE 2 : Utilisation de ModelView injecté ==========

    /**
     * Lecture et modification de la session via ModelView injecté
     * La session est automatiquement injectée dans le ModelView
     */
    @Get
    @GetURL(url = "/session/modelview/read")
    public String readSessionViaModelView(ModelView model) {
        StringBuilder result = new StringBuilder();
        result.append("<h2>Contenu de la session (via ModelView) :</h2>");
        
        Map<String, Object> session = model.getSession();
        
        if (session.isEmpty()) {
            result.append("<p>La session est vide</p>");
        } else {
            result.append("<ul>");
            for (Map.Entry<String, Object> entry : session.entrySet()) {
                result.append("<li><strong>")
                      .append(entry.getKey())
                      .append(":</strong> ")
                      .append(entry.getValue())
                      .append("</li>");
            }
            result.append("</ul>");
        }
        
        return result.toString();
    }

    /**
     * Modification de la session via ModelView
     */
    @Post
    @GetURL(url = "/session/modelview/update")
    public ModelView updateSessionViaModelView(ModelView model, 
                                               String username, 
                                               String role) {
        // Modifier directement la session du ModelView
        model.addSession("username", username);
        model.addSession("role", role);
        model.addSession("loginTime", System.currentTimeMillis());
        
        // Ajouter des données pour la vue
        model.setView("session-updated.jsp");
        model.addObject("message", "Session mise à jour via ModelView");
        
        return model;
    }

    /**
     * Vérification et gestion conditionnelle de la session
     */
    @Get
    @GetURL(url = "/session/check")
    public ModelView checkSession(ModelView model) {
        model.setView("session-check.jsp");
        
        if (model.hasSessionKey("username")) {
            String username = (String) model.getSessionObject("username");
            model.addObject("authenticated", true);
            model.addObject("username", username);
        } else {
            model.addObject("authenticated", false);
            model.addObject("message", "Veuillez vous connecter");
        }
        
        return model;
    }

    /**
     * Déconnexion - suppression de toutes les données de session
     */
    @Post
    @GetURL(url = "/session/logout")
    public ModelView logout(ModelView model) {
        // Supprimer toutes les clés importantes
        model.removeSession("username");
        model.removeSession("email");
        model.removeSession("role");
        model.removeSession("loginTime");
        
        model.setView("logout.jsp");
        model.addObject("message", "Déconnexion réussie");
        
        return model;
    }

    // ========== EXEMPLE COMBINÉ ==========

    /**
     * Utilisation combinée : @Session + autres paramètres
     */
    @Post
    @GetURL(url = "/session/login")
    public ModelView login(@Session Map<String, Object> session,
                          String username,
                          String password) {
        // Validation (simplifié pour l'exemple)
        if ("admin".equals(username) && "password".equals(password)) {
            session.put("username", username);
            session.put("authenticated", true);
            session.put("loginTime", System.currentTimeMillis());
            
            ModelView model = new ModelView("dashboard.jsp");
            model.setSession(session);
            model.addObject("message", "Bienvenue " + username);
            return model;
        } else {
            ModelView model = new ModelView("login.jsp");
            model.setSession(session);
            model.addObject("error", "Identifiants incorrects");
            return model;
        }
    }

    /**
     * Page protégée nécessitant une authentification
     */
    @Get
    @GetURL(url = "/session/protected")
    public ModelView protectedPage(ModelView model) {
        if (!model.hasSessionKey("authenticated") || 
            !(Boolean) model.getSessionObject("authenticated")) {
            
            model.setView("login.jsp");
            model.addObject("error", "Vous devez être connecté");
            return model;
        }
        
        String username = (String) model.getSessionObject("username");
        model.setView("protected-page.jsp");
        model.addObject("username", username);
        model.addObject("message", "Bienvenue sur la page protégée");
        
        return model;
    }
}