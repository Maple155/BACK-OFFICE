Team lead : Ranto ETU 3113

Dev : Dylan ETU 3175 et Faniry ETU 3149

I - Tables :
    . Parametres : 
        id (int)
        libelle (string)
        value (string)
        code (string) (Optionel)
        description (string) (optionel)

    données de test : Vitesse moyen (km / h) et temps d'attent (min)
    
    . lieu :
        id (int)
        code (string)
        libelle (string)

    . distances :
        id (int)
        from (id_lieu)
        to (id_lieu)
        kilometer (numeric)

    . vehicules_reservations : 
        id (int)
        id_voiture
        id_reservation

    . script de reinitiallisation

II - Fonctionalités 
    . Modification de la fonctionalités de creations reservations :
        * Changer la table hotel en lieu 
            (Tsy table Hotel tsony no ampiasaina fa Lieu) 
        * Modification table reservation id_hotel -> id_lieu
        * Modification entité reservation id_hotel -> id_lieu, hotelNom -> lieuCode
        * Modification de la page de creation 
        * Modification des fonctions dans ReservationService
            getReservationById(int id), 
            getAllReservationsFallback(), 
            getAllReservations(), 
            insertReservation(Reservation reservation)

            -> ze misy reference am table Hotel ovaina Lieu daoly 
        Important : Aza adino ny manova ny any am FRONT-OFFICE

    . Liste reservation en fonction d'une date donnée 
        ¤ Page 1 : page ou en selection une date 
            exemple d'affichage (fa tsy voatery ho io) : 
                input date 
                boutton 1 : voir liste assigné 
                boutton 2 : voir non assigné 

        ¤ Page 2 : liste des reservation assigné en fonction de la date choisi dans "page 1"
            (Tsy aiko oe tableau ve no affichage mety)
            Vehicule -> liste an reservation associé avec lieu hanaterana (Reservation.LieuCode)

            tables :
                vehicules
                lieu
                resevations
                vehicules_reservations
        ¤ Page 3 : Liste des reservations non assigné en fonction de la date "page 1"
            Liste an izay reservation tsy assigné am vehicule fotsiny avec lieu ihany

            tables :
                reservations
                lieu
                vehicules_reservations