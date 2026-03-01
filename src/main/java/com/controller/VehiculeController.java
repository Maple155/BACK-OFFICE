package com.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import annotation.Controller;
import annotation.Get;
import annotation.GetURL;
import annotation.JSON;
import annotation.Post;
import annotation.Param;
import com.entity.Vehicule;
import com.entity.TypeCarburant;
import com.service.TokenService;
import com.service.VehiculeService;
import service.ModelView;

@Controller
public class VehiculeController {
    
    private VehiculeService vehiculeService = new VehiculeService();
    private TokenService tokenService = new TokenService();
    // Pages JSP
    
    @Get
    @GetURL(url = "/vehicule/form")
    public ModelView vehiculeForm() {
        ModelView model = new ModelView("vehiculeForm.jsp");
        model.addObject("title", "Formulaire de vehicule");
        
        List<TypeCarburant> typesCarburant = vehiculeService.getAllTypesCarburant();
        model.addObject("typesCarburant", typesCarburant);
        
        return model;
    }
    
    @Get
    @GetURL(url = "/vehicule/list")
    public ModelView listVehicules() {
        ModelView model = new ModelView("vehiculeList.jsp");
        model.addObject("title", "Liste des vehicules");
        
        List<Vehicule> vehicules = vehiculeService.getAllVehicules();
        model.addObject("vehicules", vehicules);
        
        // Add types de carburant for modal form
        List<TypeCarburant> typesCarburant = vehiculeService.getAllTypesCarburant();
        model.addObject("typesCarburant", typesCarburant);
        
        return model;
    }
    
    @Get
    @GetURL(url = "/vehicule/edit/{id}")
    public ModelView editVehicule(@Param("id") int id) {
        ModelView model = new ModelView("vehiculeForm.jsp");
        model.addObject("title", "Modifier le vehicule");
        
        Vehicule vehicule = vehiculeService.getVehiculeById(id);
        if (vehicule != null) {
            model.addObject("vehicule", vehicule);
        }
        
        List<TypeCarburant> typesCarburant = vehiculeService.getAllTypesCarburant();
        model.addObject("typesCarburant", typesCarburant);
        
        return model;
    }
    
    // Actions CRUD
    
    @Post
    @GetURL(url = "/vehicule/save")
    public ModelView saveVehicule(
            @Param("id") Integer id,
            @Param("reference") String reference,
            @Param("nbPlaces") int nbPlaces,
            @Param("typeCarburantId") int typeCarburantId) {
        
        ModelView model = new ModelView("vehiculeForm.jsp");
        
        try {
            Vehicule vehicule = new Vehicule();
            vehicule.setReference(reference);
            vehicule.setNbPlaces(nbPlaces);
            vehicule.setTypeCarburantId(typeCarburantId);
            
            boolean success;
            
            if (id != null && id > 0) {
                // Mise à jour
                vehicule.setId(id);
                success = vehiculeService.updateVehicule(vehicule);
                model.addObject("successMessage", "Vehicule modifié avec succes!");
            } else {
                // Insertion
                success = vehiculeService.insertVehicule(vehicule);
                model.addObject("successMessage", "Vehicule créé avec succes!");
            }
            
            if (!success) {
                throw new Exception("Échec de l'operation sur la base de donnees");
            }
            
        } catch (Exception e) {
            model.addObject("errorMessage", "Erreur: " + e.getMessage());
        }
        
        List<TypeCarburant> typesCarburant = vehiculeService.getAllTypesCarburant();
        model.addObject("typesCarburant", typesCarburant);
        
        return model;
    }
    
    @Get
    @GetURL(url = "/vehicule/delete/{id}")
    public ModelView deleteVehicule(@Param("id") int id) {
        ModelView model = new ModelView("vehiculeList.jsp");
        
        boolean success = vehiculeService.deleteVehicule(id);
        if (success) {
            model.addObject("successMessage", "Véhicule supprime avec succes!");
        } else {
            model.addObject("errorMessage", "Erreur lors de la suppression du vehicule");
        }
        
        List<Vehicule> vehicules = vehiculeService.getAllVehicules();
        model.addObject("vehicules", vehicules);
        
        return model;
    }
    
    // API JSON
    
    @JSON
    @Get
    @GetURL(url = "/api/vehicules")
    public Map<String, Object> getAllVehiculesApi() {
        Map<String, Object> response = new HashMap<>();
        List<Vehicule> vehicules = vehiculeService.getAllVehicules();
        
        response.put("success", true);
        response.put("count", vehicules != null ? vehicules.size() : 0);
        response.put("vehicules", vehicules);
        
        return response;
    }
    
    @JSON
    @Get
    @GetURL(url = "/api/vehicules/{id}")
    public Map<String, Object> getVehiculeByIdApi(@Param("id") int id) {
        Map<String, Object> response = new HashMap<>();
        Vehicule vehicule = vehiculeService.getVehiculeById(id);
        
        if (vehicule != null) {
            response.put("success", true);
            response.put("vehicule", vehicule);
        } else {
            response.put("success", false);
            response.put("message", "Véhicule non trouve avec ID: " + id);
        }
        
        return response;
    }
    
    @JSON
    @Post
    @GetURL(url = "/api/vehicules/create")
    public Map<String, Object> createVehiculeApi(
            @Param("reference") String reference,
            @Param("nbPlaces") int nbPlaces,
            @Param("typeCarburantId") int typeCarburantId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Vehicule vehicule = new Vehicule(reference, nbPlaces, typeCarburantId);
            boolean success = vehiculeService.insertVehicule(vehicule);
            
            if (success) {
                response.put("success", true);
                response.put("message", "Vehicule cree avec succes");
                response.put("vehicule", vehiculeService.getVehiculeById(vehicule.getId()));
            } else {
                response.put("success", false);
                response.put("message", "Echec de la creation du vehicule");
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur: " + e.getMessage());
        }
        
        return response;
    }
    
    @JSON
    @Get
    @GetURL(url = "/api/types-carburant")
    public Map<String, Object> getAllTypesCarburantApi() {
        Map<String, Object> response = new HashMap<>();
        List<TypeCarburant> types = vehiculeService.getAllTypesCarburant();
        
        response.put("success", true);
        response.put("count", types != null ? types.size() : 0);
        response.put("typesCarburant", types);
        
        return response;
    }

    @JSON
    @Get
    @GetURL(url = "/api/types-carburant/{token}")
    public Map<String, Object> getAllTypesCarburantApiProtected(@Param("token") String token) {
        Map<String, Object> response = new HashMap<>();
        List<TypeCarburant> types = vehiculeService.getAllTypesCarburant();
        
        UUID tokenUUID = UUID.fromString(token);

        if (tokenService.checkToken(tokenUUID)) {
            response.put("success", true);
            response.put("count", types != null ? types.size() : 0);
            response.put("typesCarburant", types);
        } else {
            response.put("success", false);
            response.put("message", "Token invalide ou expire");
        }

        return response;
    }
}