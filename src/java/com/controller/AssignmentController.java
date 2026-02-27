package com.controller;

import annotation.Controller;
import annotation.GetURL;
import annotation.Post;
import annotation.Param;
import com.entity.Reservation;
import com.service.AssignmentService;
import com.service.ReservationService;
import service.ModelView;

import java.time.LocalDate;
import java.util.List;

@Controller
public class AssignmentController {

    private AssignmentService assignmentService = new AssignmentService();
    private ReservationService reservationService = new ReservationService();

    @Post
    @GetURL(url = "/reservation/assign-auto")
    public ModelView assignAutomatically(
            @Param("dateDebut") String dateDebut, 
            @Param("dateFin") String dateFin) {
        
        ModelView model = new ModelView("reservationDateFilter.jsp");
        model.addObject("title", "Liste des réservations par date");
        try {
            LocalDate debut = LocalDate.parse(dateDebut);
            LocalDate fin = LocalDate.parse(dateFin);
            
            // On récupère les réservations sur la plage sélectionnée
            List<Reservation> toAssign = reservationService.getUnassignedReservationsByDateRange(debut, fin);
            
            int count = 0;
            if (toAssign != null) {
                for (Reservation res : toAssign) {
                    // Ton service gère la logique de création ou récupération de Mission
                    boolean success = assignmentService.assignerReservationAutomatiquement(res.getId());
                    if (success) count++;
                }
            }
            
            System.out.println(count + " réservations traitées du " + dateDebut + " au " + dateFin);
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'assignation auto : " + e.getMessage());
            e.printStackTrace();
        }
        
        return model;
    }
}