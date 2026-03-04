package com.controller;

import annotation.Controller;
import annotation.GetURL;
import annotation.Post;
import annotation.Param;
import com.service.AssignmentService;
import service.ModelView;

import java.time.LocalDate;

@Controller
public class AssignmentController {

    private AssignmentService assignmentService = new AssignmentService();
    @Post
    @GetURL(url = "/reservation/assign-auto")
    public ModelView assignAutomatically(
            @Param("dateDebut") String dateDebut, 
            @Param("dateFin") String dateFin) {
        
        ModelView model = new ModelView("reservationDateFilter.jsp");
        try {
            LocalDate debut = LocalDate.parse(dateDebut);
            LocalDate fin = LocalDate.parse(dateFin);
            
            // APPEL UNIQUE AU SERVICE : C'est lui qui gère le tri et la boucle
            assignmentService.traiterReservationsEnAttente(debut.atStartOfDay(), fin.atTime(23, 59));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return model;
    }
}