Team lead : Faniry ETU 3149

Dev : Dylan ETU 3175 et Ranto ETU 3113

I - Fonctionalités 
. Assignation vehicule
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