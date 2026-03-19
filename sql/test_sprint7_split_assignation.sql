BEGIN;

-- 1. NETTOYAGE
TRUNCATE TABLE Vehicules_Reservations, Mission, Distances, Reservation, Vehicule, TypeCarburant, Lieu, Parametres CASCADE;

-- 2. PARAMÈTRES
INSERT INTO Parametres (id, libelle, value, code) VALUES
(1, 'Vitesse moyenne (km/h)', '40', 'VITESSE_MOYENNE_KMH'),
(2, 'Temps d''attente (min)', '15', 'TEMPS_ATTENTE_MIN');

-- 3. LIEUX
INSERT INTO Lieu (id, code, libelle) VALUES
(1, 'IVT', 'Ivato Aéroport'), (2, 'TNR', 'Tanà Centre'), (3, 'MAH', 'Mahamasina'),
(4, 'ALA', 'Alarobia'), (5, 'IVA', 'Ivandry'), (6, 'TAL', 'Talatamaty');

INSERT INTO Distances (id_from, id_to, kilometer) VALUES
(1, 6, 4.5), (1, 5, 12.0), (1, 2, 15.0), (1, 3, 17.0);

-- 4. FLOTTE (20 places au total)
INSERT INTO TypeCarburant (id, libelle) VALUES (1, 'Essence'), (2, 'Diesel');
INSERT INTO Vehicule (id, reference, nbPlaces, typeCarburant_id) VALUES
(1, 'VH-001-MINI', 4, 2), 
(2, 'VH-002-MID', 7, 1),  
(3, 'VH-003-MAX', 9, 2);  

-- ==========================================================
-- SCÉNARIO GÉNÉRATEUR DE RELIQUATS
-- ==========================================================

-- --- VAGUE 1 (07:30) ---
INSERT INTO Reservation (id, id_lieu, client, nbPassager, dateHeure) VALUES
(1, 6, 'R-0730-START', 4, '2026-03-19 07:30:00'),
(9, 2, 'R-SPLIT-GIGANT', 15, '2026-03-19 07:30:00'), -- Va forcer le split massif
(2, 2, 'R-0745-SUITE', 12, '2026-03-19 07:45:00'),
(3, 5, 'R-0755-FINVAGUE', 8, '2026-03-19 07:55:00');

-- --- VAGUE 2 (09:00) ---
-- Test de récupération des nombreux reliquats créés à 07h30.
INSERT INTO Reservation (id, id_lieu, client, nbPassager, dateHeure) VALUES
(4, 3, 'SOLO-SPLIT-10', 10, '2026-03-19 09:00:00');

-- 5. SÉQUENCES
SELECT setval('reservation_id_seq', (SELECT MAX(id) FROM Reservation));
COMMIT;