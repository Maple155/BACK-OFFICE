-- Nettoyage des données existantes
TRUNCATE TABLE Vehicules_Reservations, Mission, Distances, Reservation, Vehicule, TypeCarburant, Lieu, Parametres, Tokens CASCADE;

-- 1. Paramètres
INSERT INTO Parametres (libelle, value, code) VALUES
('Vitesse moyen (km / h)', '40', 'VITESSE_MOYENNE_KMH'),
('Temps d''attente (min)', '15', 'TEMPS_ATTENTE_MIN');

-- 2. Lieux
INSERT INTO Lieu (id, code, libelle) VALUES
(1, 'IVT', 'Ivato Aéroport'),
(2, 'TNR', 'Tanà Centre'),
(3, 'ANL', 'Analakely'),
(4, 'AMH', 'Ambohimanarina'),
(5, 'AMB', 'Ambatobe');

-- 3. Réservations
INSERT INTO Reservation (id, id_lieu, client, nbPassager, dateHeure) VALUES
(1, 3, '4631', 11, '2026-02-05 00:01'),
(2, 3, '4394', 1,  '2026-02-05 23:55'),
(3, 1, '8054', 2,  '2026-02-09 10:17'),
(4, 2, '1432', 4,  '2026-02-01 15:25'),
(5, 1, '7861', 4,  '2026-01-28 07:11');

-- 4. Distances
INSERT INTO Distances (id_from, id_to, kilometer) VALUES
(1, 2, 16.50), (1, 3, 17.20), (2, 3, 2.80), (2, 4, 8.10), (3, 5, 9.75), (4, 5, 6.90);

-- 5. Flotte
INSERT INTO TypeCarburant (id, libelle) VALUES
(1, 'Essence'), (2, 'Diesel'), (3, 'Electrique');

INSERT INTO Vehicule (id, reference, nbPlaces, typeCarburant_id) VALUES
(1, 'VH-001', 4, 1), (2, 'VH-002', 7, 2), (5, 'VH-005', 8, 2);

-- 6. Missions (Sans colonne statut)
INSERT INTO Mission (id, id_vehicule, heure_arrivee_aero, heure_depart_prevu, heure_retour_prevu) VALUES
(1, 1, '2026-02-04 23:46', '2026-02-05 00:01', '2026-02-05 01:00'),
(2, 2, '2026-02-09 10:02', '2026-02-09 10:17', '2026-02-09 11:30');

-- 7. Assignations
INSERT INTO Vehicules_Reservations (id_mission, id_reservation) VALUES (1, 1), (2, 3);

-- Reset et Mise à jour des séquences
SELECT setval('lieu_id_seq', COALESCE(MAX(id), 1)) FROM Lieu;
SELECT setval('reservation_id_seq', COALESCE(MAX(id), 1)) FROM Reservation;
SELECT setval('typecarburant_id_seq', COALESCE(MAX(id), 1)) FROM TypeCarburant;
SELECT setval('vehicule_id_seq', COALESCE(MAX(id), 1)) FROM Vehicule;
SELECT setval('mission_id_seq', COALESCE(MAX(id), 1)) FROM Mission;
SELECT setval('parametres_id_seq', COALESCE(MAX(id), 1)) FROM Parametres;