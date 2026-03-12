-- Jeu de données de test : mêmes date/heure, lieux différents
-- Objectif : valider la logique d'assignation/pooling des réservations.
--
-- Utilisation :
--   psql "<votre_url_postgres>" -f test_assignation_memes_dates_lieux_differents.sql

BEGIN;

-- Nettoyage des données métier
TRUNCATE TABLE Vehicules_Reservations, Mission, Distances, Reservation, Vehicule, TypeCarburant, Lieu, Parametres, Tokens CASCADE;

-- Paramètres attendus par AssignmentService
INSERT INTO Parametres (id, libelle, value, code) VALUES
(1, 'Vitesse moyen (km / h)', '40', 'VITESSE_MOYENNE_KMH'),
(2, 'Temps d''attente (min)', '0', 'TEMPS_ATTENTE_MIN');

-- Lieux (IMPORTANT : id=1 = aéroport, utilisé en dur dans AssignmentService)
INSERT INTO Lieu (id, code, libelle) VALUES
(1, 'IVT', 'Ivato Aéroport'),
(2, 'TNR', 'Tanà Centre'),
(3, 'ANL', 'Analakely'),
(4, 'AMH', 'Ambohimanarina'),
(5, 'AMB', 'Ambatobe'),
(6, 'ITA', 'Itaosy');

-- Distances minimales nécessaires pour les calculs de circuit
INSERT INTO Distances (id_from, id_to, kilometer) VALUES
(1, 2, 16.5),
(1, 3, 17.2),
(1, 4, 12.0),
(1, 5, 19.3),
(1, 6, 22.0),
(2, 3, 2.8),
(2, 4, 8.1),
(2, 5, 9.7),
(3, 4, 7.5),
(3, 5, 9.1),
(4, 5, 6.9),
(5, 6, 11.2),
(4, 6, 13.0);

-- Types de carburant
INSERT INTO TypeCarburant (id, libelle) VALUES
(1, 'Essence'),
(2, 'Diesel');

-- Flotte de test (inclut Diesel pour la règle de priorité)
INSERT INTO Vehicule (id, reference, nbPlaces, typeCarburant_id) VALUES
(1, 'VH-TEST-001', 4, 1),
(2, 'VH-TEST-002', 6, 2),
(3, 'VH-TEST-003', 8, 2);

-- Cas principal demandé : même date/heure EXACTE, lieux différents
-- Toutes ces réservations sont à 2026-03-10 09:00:00 mais destinations différentes
INSERT INTO Reservation (id, id_lieu, client, nbPassager, dateHeure) VALUES
(1, 2, 'CLI-1001', 2, '2026-03-10 09:00:00'),
(2, 3, 'CLI-1002', 1, '2026-03-10 09:00:00'),
(3, 4, 'CLI-1003', 2, '2026-03-10 09:00:00'),
(4, 5, 'CLI-1004', 1, '2026-03-10 09:00:00'),
(5, 6, 'CLI-1005', 7, '2026-03-10 09:00:00');

-- Données complémentaires optionnelles (autre créneau)
INSERT INTO Reservation (id, id_lieu, client, nbPassager, dateHeure) VALUES
(6, 3, 'CLI-2001', 2, '2026-03-10 10:30:00'),
(7, 5, 'CLI-2002', 1, '2026-03-10 10:30:00');

-- Remise à niveau des séquences
SELECT setval('parametres_id_seq', COALESCE((SELECT MAX(id) FROM Parametres), 1));
SELECT setval('lieu_id_seq', COALESCE((SELECT MAX(id) FROM Lieu), 1));
SELECT setval('typecarburant_id_seq', COALESCE((SELECT MAX(id) FROM TypeCarburant), 1));
SELECT setval('vehicule_id_seq', COALESCE((SELECT MAX(id) FROM Vehicule), 1));
SELECT setval('reservation_id_seq', COALESCE((SELECT MAX(id) FROM Reservation), 1));

COMMIT;

-- Vérifications rapides
-- 1) Toutes les réservations du cas principal ont la même date/heure
SELECT id, id_lieu, client, nbPassager, dateHeure
FROM Reservation
WHERE dateHeure = '2026-03-10 09:00:00'
ORDER BY id;

-- 2) Contrôle visuel des lieux
SELECT r.id, r.client, l.code AS lieu_code, l.libelle, r.dateHeure
FROM Reservation r
JOIN Lieu l ON l.id = r.id_lieu
ORDER BY r.dateHeure, r.id;
