package com.controller;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import annotation.Controller;
import annotation.Get;
import annotation.GetURL;
import annotation.JSON;
import annotation.Post;
import annotation.Param;
import com.entity.Reservation;
import com.entity.Lieu;
import com.service.ReservationService;
import service.ModelView;

@Controller
public class ReservationController {
    
    private ReservationService reservationService = new ReservationService();
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
 
    @Get
    @GetURL(url = "/reservation/form")
    public ModelView reservationForm() {
        ModelView model = new ModelView("reservationForm.jsp");
        model.addObject("title", "Formulaire de reservation");
        
        List<Lieu> lieux = reservationService.getAllLieux();
        model.addObject("lieux", lieux);
        
        return model;
    }

    @Post
    @GetURL(url = "/reservation/save")
    public ModelView saveReservation(
            @Param("idLieu") int idLieu,
            @Param("client") String client,
            @Param("nbPassager") int nbPassager,
            @Param("dateHeure") String dateHeureStr) {
        
        try {
            LocalDateTime dateHeure = LocalDateTime.parse(dateHeureStr);
            
            Reservation reservation = new Reservation(idLieu, client, nbPassager, dateHeure);
            
            boolean success = reservationService.insertReservation(reservation);
            
            if (success) {
                ModelView model = new ModelView("reservationForm.jsp");
                model.addObject("successMessage", "Réservation creee avec succes!");
                model.addObject("lieux", reservationService.getAllLieux());
                return model;
            } else {
                throw new Exception("Échec de l'insertion dans la base de données");
            }
            
        } catch (Exception e) {
            ModelView model = new ModelView("reservationForm.jsp");
            model.addObject("errorMessage", "Erreur lors de la création de la réservation: " + e.getMessage());
            
            List<Lieu> lieux = reservationService.getAllLieux();
            model.addObject("lieux", lieux);
            
            return model;
        }
    }

    @Get
    @GetURL(url = "/reservation/list")
    public ModelView listReservations() {
        ModelView model = new ModelView("reservationList.jsp");
        model.addObject("title", "Liste des reservations");
        
        List<Reservation> reservations = reservationService.getAllReservations();
        model.addObject("reservations", reservations);
        
        return model;
    }

    @Get
    @GetURL(url = "/reservation/date/filter")
    public ModelView reservationDateFilter() {
        ModelView model = new ModelView("reservationDateFilter.jsp");
        model.addObject("title", "Liste des réservations par date");
        return model;
    }

    @Get
    @GetURL(url = "/reservation/date/assigned")
    public ModelView listAssignedReservationsByDate(@Param("date") String dateStr) {
        ModelView model = new ModelView("reservationAssignedList.jsp");
        model.addObject("title", "Réservations assignées");
        model.addObject("selectedDate", dateStr);

        try {
            LocalDate date = LocalDate.parse(dateStr);
            List<Map<String, Object>> assigned = reservationService.getAssignedReservationsByDate(date);
            model.addObject("assignedReservations", assigned);
        } catch (Exception e) {
            model.addObject("errorMessage", "Date invalide: " + e.getMessage());
            model.addObject("assignedReservations", new ArrayList<>());
        }

        return model;
    }

    @Get
    @GetURL(url = "/reservation/date/unassigned")
    public ModelView listUnassignedReservationsByDate(@Param("date") String dateStr) {
        ModelView model = new ModelView("reservationUnassignedList.jsp");
        model.addObject("title", "Réservations non assignées");
        model.addObject("selectedDate", dateStr);

        try {
            LocalDate date = LocalDate.parse(dateStr);
            List<Reservation> unassigned = reservationService.getUnassignedReservationsByDate(date);
            model.addObject("unassignedReservations", unassigned);
        } catch (Exception e) {
            model.addObject("errorMessage", "Date invalide: " + e.getMessage());
            model.addObject("unassignedReservations", new ArrayList<>());
        }

        return model;
    }

    @Get
    @GetURL(url = "/reservation/delete/{id}")
    public ModelView deleteReservation(@Param("id") int id) {
        ModelView model = new ModelView("reservationForm.jsp");
        
        boolean success = reservationService.deleteReservation(id);
        if (success) {
            model.addObject("successMessage", "Reservation supprimee avec succes!");
        } else {
            model.addObject("errorMessage", "Erreur lors de la suppression de la réservation");
        }
        
        return model;
    }

@JSON
    @Get
    @GetURL(url = "/api/reservations")
    public Map<String, Object> getAllReservationsApi() {
        System.out.println("=== Appel de getAllReservationsApi() ===");
        
        Map<String, Object> response = new HashMap<>();
        List<Reservation> reservations = reservationService.getAllReservations();
        
        response.put("success", true);
        response.put("count", reservations != null ? reservations.size() : 0);
        
        // Convertir les réservations en liste de Maps
        List<Map<String, Object>> reservationsList = new ArrayList<>();
        if (reservations != null) {
            for (Reservation reservation : reservations) {
                reservationsList.add(convertReservationToMap(reservation));
            }
        }
        response.put("reservations", reservationsList);
        
        return response;
    }

    @JSON
    @Get
    @GetURL(url = "/api/reservations/{id}")
    public Map<String, Object> getReservationByIdApi(@Param("id") int id) {
        System.out.println("=== Appel de getReservationByIdApi() - ID: " + id + " ===");
        
        Map<String, Object> response = new HashMap<>();
        Reservation reservation = reservationService.getReservationById(id);
        
        if (reservation != null) {
            response.put("success", true);
            response.put("reservation", convertReservationToMap(reservation));
        } else {
            response.put("success", false);
            response.put("message", "Réservation non trouvée avec ID: " + id);
        }
        
        return response;
    }

    @JSON
    @Post
    @GetURL(url = "/api/reservations/create")
    public Map<String, Object> createReservationApi(
            @Param("idLieu") int idLieu,
            @Param("client") String client,
            @Param("nbPassager") int nbPassager,
            @Param("dateHeure") String dateHeureStr) {
        
        System.out.println("=== Appel de createReservationApi() ===");
        System.out.println("Paramètres API reçus:");
        System.out.println("  idLieu: " + idLieu);
        System.out.println("  client: " + client);
        System.out.println("  nbPassager: " + nbPassager);
        System.out.println("  dateHeure: " + dateHeureStr);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            LocalDateTime dateHeure = LocalDateTime.parse(dateHeureStr);
            
            Reservation reservation = new Reservation(idLieu, client, nbPassager, dateHeure);
            
            boolean success = reservationService.insertReservation(reservation);
            
            if (success) {
                response.put("success", true);
                response.put("message", "Réservation créée avec succès");
                response.put("reservation", convertReservationToMap(reservation));
            } else {
                response.put("success", false);
                response.put("message", "Échec de la creation de la reservation");
            }
            
        } catch (Exception e) {
            System.out.println("Erreur API: " + e.getMessage());
            response.put("success", false);
            response.put("message", "Erreur: " + e.getMessage());
        }
        
        return response;
    }

    @JSON
    @Get
    @GetURL(url = "/api/lieux")
    public Map<String, Object> getAllLieuxApi() {
        System.out.println("=== Appel de getAllLieuxApi() ===");
        
        Map<String, Object> response = new HashMap<>();
        List<Lieu> lieux = reservationService.getAllLieux();
        
        response.put("success", true);
        response.put("count", lieux != null ? lieux.size() : 0);
        
        List<Map<String, Object>> lieuxList = new ArrayList<>();
        if (lieux != null) {
            for (Lieu lieu : lieux) {
                Map<String, Object> lieuMap = new HashMap<>();
                lieuMap.put("id", lieu.getId());
                lieuMap.put("code", lieu.getCode());
                lieuMap.put("libelle", lieu.getLibelle());
                lieuxList.add(lieuMap);
            }
        }
        response.put("lieux", lieuxList);
        
        return response;
    }

    @JSON
    @Get
    @GetURL(url = "/api/stats")
    public Map<String, Object> getStatsApi() {
        System.out.println("=== Appel de getStatsApi() ===");
        
        Map<String, Object> response = new HashMap<>();
        List<Reservation> reservations = reservationService.getAllReservations();
        
        int totalReservations = reservations != null ? reservations.size() : 0;
        int totalPassagers = 0;
        Map<Integer, Integer> reservationsParLieu = new HashMap<>();
        
        if (reservations != null) {
            for (Reservation reservation : reservations) {
                totalPassagers += reservation.getNbPassager();
                int lieuId = reservation.getIdLieu();
                reservationsParLieu.put(lieuId, reservationsParLieu.getOrDefault(lieuId, 0) + 1);
            }
        }
        
        response.put("success", true);
        response.put("totalReservations", totalReservations);
        response.put("totalPassagers", totalPassagers);
        response.put("reservationsParLieu", reservationsParLieu);
        
        return response;
    }

    // Méthode utilitaire pour convertir une réservation en Map
    private Map<String, Object> convertReservationToMap(Reservation reservation) {
        Map<String, Object> map = new HashMap<>();
        
        if (reservation != null) {
            map.put("id", reservation.getId());
            map.put("idLieu", reservation.getIdLieu());
            map.put("client", reservation.getClient());
            map.put("nbPassager", reservation.getNbPassager());
            map.put("lieuCode", reservation.getLieuCode());
            
            // Formater les dates
            if (reservation.getDateHeure() != null) {
                map.put("dateHeure", reservation.getDateHeure().format(formatter));
                map.put("dateHeureDisplay", reservation.getDateHeure().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                map.put("dateHeureISO", reservation.getDateHeure().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }
            
            // Informations supplémentaires
            map.put("createdAt", java.time.LocalDateTime.now().format(formatter));
        }
        
        return map;
    }
}