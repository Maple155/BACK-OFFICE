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

    . Assignation vehicule (optionnel) 
        Tsy aiko oe automatique ve sa atao tanana 
        . Regle de gestion pour l'assignation d'une reservation à une vehicule 
            1 - Raha misy olona 8 (reservation.nbPassager) pour un reservation dia izay vehicule >= 8 ny nbPlaces no alaina 
            2 - Raha bdb ny vehicule mi-satisfaire an ilay regles 1 dia izay vehicule proche an ilay 8 no alaina 
                exemple : raha misy vehicule 2 -> 1 = 8 places, 2 = 11 places dia ilay 8 places no alaina
            3 - Raha bdb ka samy 8 dia izay typeCarburant = Diesel no alaina 
            4 - Raha samy diesel de random
            5 - Raha misy reservation maromaro ao anatin temps d'attent izany oe 
                    exemple : 
                        res1.dateHeure = 22/02/2026 4H20 
                        res2.dateHeure = 22/02/2026 4H30
                        res3.dateHeure = 22/02/2026 4H10

                        temps d'attent 30 min ka nisy vehicule eo am 4h alors 4h + temps de pause = 4h30 satria 
                            temps de depart = temps arrivé vehicule + temps d'attent
                            temps d'arrivé = calculer-na à partir an table distance.kilometer avec vitesse moyen ao am table parameters

                            NB (Mbola tsy atao am ty sprint 3) : raha misy lieu maromaro andehanan vehicule iray dia izay akaikin'ny aeroport no andehanana voalohany dia avy eo à partir an le toerana farany dia andehanana indray izy akaikiny et ainsi de suite. dia refa vita dia caluler ny distance parcouru de calculer amzay ny temps d'arrivé Fa raha misy ao am table distance from/to (aeroport) to/from (distance finale) dia tonga dia iny no ampiasaina fa tsy mila manao calcule tsony 

                        donc lasa tafiditra ao daoly res1, res2, res3 DONC 
                            - miaraka traiter-na izany oe miaraka hitadiavana voiture miaraka antonona (res1 + res2 + res3).nbPassager
                            - raha tsisy vehicule mahazaka an zareo dia ny reservation manana nbPassager ambony indrindra no traier-na en priorité