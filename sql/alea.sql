BEGIN;

-- 1. NETTOYAGE
TRUNCATE TABLE Vehicules_Reservations, Mission, Distances, Reservation, Vehicule, TypeCarburant, Lieu, Parametres CASCADE;

-- 2. PARAMÈTRES
INSERT INTO Parametres (id, libelle, value, code) VALUES
(1, 'Vitesse moyenne (km/h)', '50', 'VITESSE_MOYENNE_KMH'),
(2, 'Temps d''attente (min)', '30', 'TEMPS_ATTENTE_MIN');

-- 3. LIEUX
INSERT INTO Lieu (id, code, libelle) VALUES
(1, 'AERO', 'Ivato Aéroport'), (2, 'HT1', 'Tanà Centre'), (3, 'HT2', 'Mahamasina'),
(4, 'ALA', 'Alarobia'), (5, 'IVA', 'Ivandry'), (6, 'TAL', 'Talatamaty');

INSERT INTO Distances (id_from, id_to, kilometer) VALUES
(1, 2, 90.0), (1, 3, 35.0), (2, 3, 60.0);

-- 4. FLOTTE (20 places au total)
INSERT INTO TypeCarburant (id, libelle) VALUES (1, 'Essence'), (2, 'Diesel');
INSERT INTO Vehicule (id, reference, nbPlaces, heure_debut_disponibilite, typeCarburant_id) VALUES
(1, 'VH-001-MINI', 5, '09:00:00', 2),
(2, 'VH-002-MID', 5, '09:00:00', 1),
(3, 'VH-003-MID', 12, '00:00:00', 2),
(4, 'VH-004-MAX', 9, '09:00:00', 2),
(5, 'VH-005-MAX', 12, '13:00:00', 1);

-- ==========================================================
-- SCÉNARIO GÉNÉRATEUR DE RELIQUATS
-- ==========================================================

-- --- VAGUE 1 (07:30) ---
INSERT INTO Reservation (id, id_lieu, client, nbPassager, dateHeure) VALUES
(1, 2, 'c1', 7, '2026-03-19 09:00:00'),
(2, 3, 'c2', 20, '2026-03-19 08:00:00'), -- Va forcer le split massif
(3, 2, 'c3', 3, '2026-03-19 09:10:00'),
(4, 2, 'c4', 10, '2026-03-19 09:15:00'), -- Va forcer le split massif
(5, 2, 'c5', 5, '2026-03-19 09:20:00'),
(6, 2, 'c6', 12, '2026-03-19 13:30:00'); -- Va forcer le split massif

COMMIT;