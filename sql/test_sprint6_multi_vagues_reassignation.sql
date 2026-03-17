-- Jeu de données complet Sprint 6
-- Objectif : valider les nouvelles règles d'assignation multi-vagues / multi-trajets
--
-- Règles couvertes :
-- 1) Plusieurs vagues par journée (pas une seule vague figée)
-- 2) Un véhicule de retour dans une nouvelle vague peut être réutilisé
-- 3) Priorité au plus petit nombre de trajets du jour
-- 4) A égalité de trajets : véhicule pouvant contenir toute la vague, puis capacité la plus proche, puis Diesel
-- 5) Une réservation non assignée d'une vague précédente peut rester candidate pour les traitements suivants
-- 6) Départ réel d'une mission = max(heure dernière réservation de la mission, heure disponibilité véhicule)
--
-- Utilisation:
--   1) Charger le jeu:
--      psql -U postgres -d locations -f sql/test_sprint6_multi_vagues_reassignation.sql
--   2) Lancer l'assignation auto (phase précondition) sur 2026-04-02 08:20 -> 09:30
--      (cela crée les missions "précédentes" de manière naturelle)
--   3) Lancer l'assignation auto (phase principale) sur 2026-04-02 07:00 -> 10:30
--   4) Exécuter les requêtes de vérification en bas du fichier

BEGIN;

TRUNCATE TABLE Vehicules_Reservations, Mission, Distances, Reservation, Vehicule, TypeCarburant, Lieu, Parametres, Tokens CASCADE;

-- Paramètres moteur
INSERT INTO Parametres (id, libelle, value, code) VALUES
(1, 'Vitesse moyen (km / h)', '40', 'VITESSE_MOYENNE_KMH'),
(2, 'Temps d''attente (min)', '30', 'TEMPS_ATTENTE_MIN');

-- IMPORTANT : id=1 = Aéroport
INSERT INTO Lieu (id, code, libelle) VALUES
(1, 'IVT', 'Ivato Aéroport'),
(2, 'TNR', 'Tanà Centre'),
(3, 'ANL', 'Analakely'),
(4, 'AMH', 'Ambohimanarina'),
(5, 'AMB', 'Ambatobe'),
(6, 'ITA', 'Itaosy');

-- Distances simples et cohérentes
INSERT INTO Distances (id_from, id_to, kilometer) VALUES
(1, 2, 12.0),
(1, 3, 14.0),
(1, 4, 10.0),
(1, 5, 18.0),
(1, 6, 22.0),
(2, 3, 3.0),
(2, 4, 6.0),
(2, 5, 8.0),
(2, 6, 12.0),
(3, 4, 5.0),
(3, 5, 7.0),
(3, 6, 11.0),
(4, 5, 6.0),
(4, 6, 9.0),
(5, 6, 8.0);

INSERT INTO TypeCarburant (id, libelle) VALUES
(1, 'Essence'),
(2, 'Diesel');

-- Flotte
-- v1: 4 places Diesel
-- v2: 7 places Essence
-- v3: 9 places Diesel
-- v4: 3 places Diesel
INSERT INTO Vehicule (id, reference, nbPlaces, typeCarburant_id) VALUES
(1, 'VH-001', 4, 2),
(2, 'VH-002', 7, 1),
(3, 'VH-003', 9, 2),
(4, 'VH-004', 3, 2);

-- =========================
-- PRÉCONDITION (sans insertion manuelle de Mission)
-- Ces réservations sont traitées en PHASE 1 pour générer des missions "historiques"
-- via la logique applicative elle-même.
-- On force des charges élevées pour faire travailler prioritairement le véhicule 9 places.
-- =========================
INSERT INTO Reservation (id, id_lieu, client, nbPassager, dateHeure) VALUES
(90, 6, 'PRE-0820', 9, '2026-04-02 08:20:00'),
(91, 6, 'PRE-0915', 9, '2026-04-02 09:15:00');

-- =========================
-- SCENARIO A (07:00 -> 07:30)
-- "Un seul véhicule peut contenir toute la vague"
-- Total passagers = 7 => le v2 (7 places) devrait être préféré à découpage multi-voitures
-- =========================
INSERT INTO Reservation (id, id_lieu, client, nbPassager, dateHeure) VALUES
(1, 2, 'A-0700', 4, '2026-04-02 07:00:00'),
(2, 3, 'A-0710', 2, '2026-04-02 07:10:00'),
(3, 4, 'A-0720', 1, '2026-04-02 07:20:00');

-- =========================
-- SCENARIO B (09:00 -> 09:30)
-- "Priorité au moins de trajets"
-- Le nombre de trajets dépendra des missions créées en PHASE 1.
-- Vérifier avec V3 que la répartition suit le minimum de trajets avant les autres critères.
-- =========================
INSERT INTO Reservation (id, id_lieu, client, nbPassager, dateHeure) VALUES
(4, 5, 'B-0900', 3, '2026-04-02 09:00:00');

-- =========================
-- SCENARIO C (10:00 -> 10:30)
-- Cas demandé: réservations 10:00, 10:15, 10:20 et véhicule de 10:15 dispo à 10:25
-- r5 (2p), r6 (5p), r7 (2p)
-- Les véhicules 1 et 4 ne peuvent pas porter r6 (5p)
-- Attendu métier: si le véhicule assigné à r6 revient après la dernière réservation de la mission
-- mais avant la fin théorique de vague, le départ devient l'heure de retour véhicule.
-- =========================
INSERT INTO Reservation (id, id_lieu, client, nbPassager, dateHeure) VALUES
(5, 2, 'C-1000', 2, '2026-04-02 10:00:00'),
(6, 6, 'C-1015', 5, '2026-04-02 10:15:00'),
(7, 4, 'C-1020', 2, '2026-04-02 10:20:00');

-- =========================
-- SCENARIO D
-- Réservation initialement non assignée (capacité trop grande), puis potentiellement assignable plus tard
-- Ici 10 passagers > max capacité (9) => non assignée
-- Elle reste dans le backlog sans "priorité artificielle"
-- =========================
INSERT INTO Reservation (id, id_lieu, client, nbPassager, dateHeure) VALUES
(8, 3, 'D-0835-NON-ASSIGNABLE', 10, '2026-04-02 08:35:00');

-- Ajustement séquences
SELECT setval('parametres_id_seq', COALESCE((SELECT MAX(id) FROM Parametres), 1));
SELECT setval('lieu_id_seq', COALESCE((SELECT MAX(id) FROM Lieu), 1));
SELECT setval('typecarburant_id_seq', COALESCE((SELECT MAX(id) FROM TypeCarburant), 1));
SELECT setval('vehicule_id_seq', COALESCE((SELECT MAX(id) FROM Vehicule), 1));
SELECT setval('reservation_id_seq', COALESCE((SELECT MAX(id) FROM Reservation), 1));
SELECT setval('mission_id_seq', COALESCE((SELECT MAX(id) FROM Mission), 1));

COMMIT;

-- ==========================================================
-- REQUÊTES DE VÉRIFICATION (à lancer APRÈS PHASE 1 + PHASE 2)
-- ==========================================================

-- V1) Vue générale missions + véhicules
SELECT
    m.id,
    m.id_vehicule,
    v.reference,
    v.nbPlaces,
    m.heure_arrivee_aero,
    m.heure_depart_prevu,
    m.heure_retour_prevu
FROM Mission m
JOIN Vehicule v ON v.id = m.id_vehicule
ORDER BY m.heure_arrivee_aero, m.id;

-- V2) Réservations et mission associée
SELECT
    r.id,
    r.client,
    r.nbPassager,
    r.dateHeure,
    vr.id_mission
FROM Reservation r
LEFT JOIN Vehicules_Reservations vr ON vr.id_reservation = r.id
ORDER BY r.dateHeure, r.id;

-- V3) Nombre de trajets par véhicule (contrôle de répartition)
SELECT
    v.id,
    v.reference,
    COUNT(m.id) AS nb_trajets
FROM Vehicule v
LEFT JOIN Mission m ON m.id_vehicule = v.id AND DATE(m.heure_arrivee_aero) = DATE('2026-04-02')
GROUP BY v.id, v.reference
ORDER BY nb_trajets ASC, v.id ASC;

-- V4) Contrôle ciblé scénario C : mission de la réservation C-1015
-- Vérifier: heure_depart_prevu >= reservation_heure
-- et, si le véhicule revient après reservation_heure, départ aligné sur disponibilité véhicule.
SELECT
    r.id AS reservation_id,
    r.client,
    r.dateHeure AS reservation_heure,
    m.id AS mission_id,
    m.id_vehicule,
    m.heure_depart_prevu
FROM Reservation r
JOIN Vehicules_Reservations vr ON vr.id_reservation = r.id
JOIN Mission m ON m.id = vr.id_mission
WHERE r.client = 'C-1015';

-- V5) Contrôle backlog non assigné (scénario D)
SELECT
    r.id,
    r.client,
    r.nbPassager,
    r.dateHeure
FROM Reservation r
LEFT JOIN Vehicules_Reservations vr ON vr.id_reservation = r.id
WHERE vr.id_reservation IS NULL
ORDER BY r.dateHeure, r.id;
