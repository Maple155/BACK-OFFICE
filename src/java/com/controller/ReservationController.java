package com.controller;

import java.time.LocalDateTime;
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
import com.entity.Hotel;
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
        
        List<Hotel> hotels = reservationService.getAllHotels();
        model.addObject("hotels", hotels);
        
        return model;
    }

    @Post
    @GetURL(url = "/reservation/save")
    public ModelView saveReservation(
            @Param("idHotel") int idHotel,
            @Param("client") String client,
            @Param("nbPassager") int nbPassager,
            @Param("dateHeure") String dateHeureStr) {
        
        try {
            LocalDateTime dateHeure = LocalDateTime.parse(dateHeureStr);
            
            Reservation reservation = new Reservation(idHotel, client, nbPassager, dateHeure);
            
            boolean success = reservationService.insertReservation(reservation);
            
            if (success) {
                ModelView model = new ModelView("reservationForm.jsp");
                model.addObject("successMessage", "Réservation creee avec succes!");
                return model;
            } else {
                throw new Exception("Échec de l'insertion dans la base de données");
            }
            
        } catch (Exception e) {
            ModelView model = new ModelView("reservationForm.jsp");
            model.addObject("errorMessage", "Erreur lors de la création de la réservation: " + e.getMessage());
            
            List<Hotel> hotels = reservationService.getAllHotels();
            model.addObject("hotels", hotels);
            
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
            @Param("idHotel") int idHotel,
            @Param("client") String client,
            @Param("nbPassager") int nbPassager,
            @Param("dateHeure") String dateHeureStr) {
        
        System.out.println("=== Appel de createReservationApi() ===");
        System.out.println("Paramètres API reçus:");
        System.out.println("  idHotel: " + idHotel);
        System.out.println("  client: " + client);
        System.out.println("  nbPassager: " + nbPassager);
        System.out.println("  dateHeure: " + dateHeureStr);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Convertir la date string en LocalDateTime
            LocalDateTime dateHeure = LocalDateTime.parse(dateHeureStr);
            
            // Créer la réservation
            Reservation reservation = new Reservation(idHotel, client, nbPassager, dateHeure);
            
            // Enregistrer dans la base de données
            boolean success = reservationService.insertReservation(reservation);
            
            if (success) {
                response.put("success", true);
                response.put("message", "Réservation creee avec succes");
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
    @GetURL(url = "/api/hotels")
    public Map<String, Object> getAllHotelsApi() {
        System.out.println("=== Appel de getAllHotelsApi() ===");
        
        Map<String, Object> response = new HashMap<>();
        List<Hotel> hotels = reservationService.getAllHotels();
        
        response.put("success", true);
        response.put("count", hotels != null ? hotels.size() : 0);
        
        // Convertir les hôtels en liste de Maps
        List<Map<String, Object>> hotelsList = new ArrayList<>();
        if (hotels != null) {
            for (Hotel hotel : hotels) {
                Map<String, Object> hotelMap = new HashMap<>();
                hotelMap.put("id", hotel.getId());
                hotelMap.put("nom", hotel.getNom());
                hotelsList.add(hotelMap);
            }
        }
        response.put("hotels", hotelsList);
        
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
        Map<Integer, Integer> reservationsParHotel = new HashMap<>();
        
        if (reservations != null) {
            for (Reservation reservation : reservations) {
                totalPassagers += reservation.getNbPassager();
                int hotelId = reservation.getIdHotel();
                reservationsParHotel.put(hotelId, reservationsParHotel.getOrDefault(hotelId, 0) + 1);
            }
        }
        
        response.put("success", true);
        response.put("totalReservations", totalReservations);
        response.put("totalPassagers", totalPassagers);
        response.put("reservationsParHotel", reservationsParHotel);
        
        return response;
    }

    // Méthode utilitaire pour convertir une réservation en Map
    private Map<String, Object> convertReservationToMap(Reservation reservation) {
        Map<String, Object> map = new HashMap<>();
        
        if (reservation != null) {
            map.put("id", reservation.getId());
            map.put("idHotel", reservation.getIdHotel());
            map.put("client", reservation.getClient());
            map.put("nbPassager", reservation.getNbPassager());
            map.put("hotelNom", reservation.getHotelNom());
            
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