Team lead : Dylan ETU 3175

Dev : Ranto ETU 3113 et Faniry ETU 3149

Fonctionalité : 
    
    Consideration nombre de trajet et retour de chaque vehicule :
        
        .   Ny vehicule izay efa niverina dia efa candidat ho an interval manaraka 
            
            * ex :    premier vol 8h avec temps d'attente 30 min donc max depart à 8h 30 c-à-d 8h -> 8h 30 ny interval voalohany ka misy vehicule assigné tao ka ohatra oe misy interval ex (10h -> 10h 30 ) indray ka raha tafaverina izy ao anatin io interval io dia azo reassigner-na amin'ny reservation indray izany izy
                    
        .   Izay vehicule moins de trajet no candidat voalohany (priorité) sinon manaraka ny regles taloha 

        .   Raha misy 2 ou + moins de trajet dia diesel sinon random 

        .   Vol tonga voalohany no misy temps d'attente voalohany izany oe :
            
            Raha ny vol tonga teo am aeroport tamin' ny 8h, 8h 15, 9h, 9h 30 dia ilay 8h + temps d'attente izy 

            * Raha misy vol tsy ao anatin ilay interval voalohany  (vol voalohany -> vol voalohany + temps d'attente) dia izay vol aloha indrindra ao indray no ampiana temps d'attente et ainsi de suite 

                ex : vols : 8h, 8h 15, 9h, 9h 30 ; temps d'attente : 30 min
                    -> interval 1 (8h -> 8h 30), interval 2 (9h -> 9h 30)

            izay reservation rehetra même interval no miaraka hitadiavana vehicule si possible sinon sarahana ny reservation sinon non assigner (ohatran taloha ihany) 
    
        .   Raha ohatra oe misy reservation non assigner tao amin' ny reservation 1 dia atsofoka ao amin' ny interval manaraka fa tsy prioritaire ilay reservation 

        . Ny vol farany no depart farany ao anatin ilay interval no depart final 

            * Fa raha ohatra oe :

                vols : 10h, 10h 15, 10h20; temps d'attente : 30 min 
                vehicule dispo: v1 (efa teo), v2 (tonga tam 10h 25)
                interval : 10h -> 10h 30

                => vol 10h et 10h 20 tafiditra tao am v1 fa 10h15 tsy tafidira dia raha oe vol farany no jerena dia depart 10h 20 fa nefa nisy vehicule v2 antonona an 10h 15 donc depart zany 10h 25 satria mbola ao anatin ilay interval

        . ❤️                   