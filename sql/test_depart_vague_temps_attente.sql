-- Test ciblé : départ de vague ajusté à la dernière réservation <= (première réservation + temps d'attente)
--
-- Scénario:
--   - TEMPS_ATTENTE_MIN = 30
--   - Première réservation = 08:00
--   - Fin théorique d'attente = 08:30
--   - Réservation la plus proche <= 08:30 = 08:20
-- Attendu:
--   - Toutes les missions de la vague partent à 08:20 (et non 08:30)
--   - Une réservation à 08:45 reste non assignée (règle vague unique)
--
-- Utilisation:
--   1) Charger ce jeu de données
--      psql -U postgres -d locations -f sql/test_depart_vague_temps_attente.sql
--   2) Lancer l'assignation automatique côté app (dateDebut=dateFin=2026-03-17)
--      (ex: endpoint /reservation/assign-auto)
--   3) Exécuter les requêtes de vérification en bas du fichier

BEGIN;

TRUNCATE TABLE Vehicules_Reservations, Mission, Distances, Reservation, Vehicule, TypeCarburant, Lieu, Parametres, Tokens CASCADE;

-- Paramètres utilisés par AssignmentService
INSERT INTO Parametres (id, libelle, value, code) VALUES
(1, 'Vitesse moyen (km / h)', '40', 'VITESSE_MOYENNE_KMH'),
(2, 'Temps d''attente (min)', '30', 'TEMPS_ATTENTE_MIN');

-- IMPORTANT: id=1 = aéroport (constante AEROPORT_ID)
INSERT INTO Lieu (id, code, libelle) VALUES
(1, 'IVT', 'Ivato Aéroport'),
(2, 'TNR', 'Tanà Centre'),
(3, 'ANL', 'Analakely'),
(4, 'AMH', 'Ambohimanarina');

INSERT INTO Distances (id_from, id_to, kilometer) VALUES
(1, 2, 16.5),
(1, 3, 17.2),
(1, 4, 12.0),
(2, 3, 2.8),
(2, 4, 8.1),
(3, 4, 7.5);

INSERT INTO TypeCarburant (id, libelle) VALUES
(1, 'Essence'),
(2, 'Diesel');

-- 2 petits véhicules (4 places) + 1 grand (8 places)
INSERT INTO Vehicule (id, reference, nbPlaces, typeCarburant_id) VALUES
(1, 'VH-001', 4, 2),
(2, 'VH-002', 4, 2),
(3, 'VH-003', 8, 1);

-- Réservations de la vague (même journée)
-- r1 = première réservation (ancre de vague)
-- r3 à 08:20 doit tirer le départ réel commun à 08:20
INSERT INTO Reservation (id, id_lieu, client, nbPassager, dateHeure) VALUES
(1, 2, 'CLI-0800', 4, '2026-03-17 08:00:00'),
(2, 3, 'CLI-0810', 4, '2026-03-17 08:10:00'),
(3, 4, 'CLI-0820', 1, '2026-03-17 08:20:00'),
(4, 2, 'CLI-0845', 1, '2026-03-17 08:45:00');

SELECT setval('parametres_id_seq', COALESCE((SELECT MAX(id) FROM Parametres), 1));
SELECT setval('lieu_id_seq', COALESCE((SELECT MAX(id) FROM Lieu), 1));
SELECT setval('typecarburant_id_seq', COALESCE((SELECT MAX(id) FROM TypeCarburant), 1));
SELECT setval('vehicule_id_seq', COALESCE((SELECT MAX(id) FROM Vehicule), 1));
SELECT setval('reservation_id_seq', COALESCE((SELECT MAX(id) FROM Reservation), 1));

COMMIT;

-- =========================
-- Vérifications post-assignation
-- =========================
-- Lancer ces requêtes APRES appel de /reservation/assign-auto sur 2026-03-17

-- 1) Toutes les missions de la vague doivent avoir le même départ réel = 08:20
SELECT
    m.id,
    m.id_vehicule,
    m.heure_arrivee_aero,
    m.heure_depart_prevu,
    m.heure_retour_prevu
FROM Mission m
ORDER BY m.id;

-- 2) Contrôle de l'assignation des réservations
SELECT
    r.id,
    r.client,
    r.dateHeure,
    vr.id_mission
FROM Reservation r
LEFT JOIN Vehicules_Reservations vr ON vr.id_reservation = r.id
ORDER BY r.id;

-- Attendus:
-- - r1, r2, r3 assignées (id_mission non null)
-- - r4 (08:45) non assignée (id_mission null)
-- - toutes les missions créées pour la vague ont heure_depart_prevu = 2026-03-17 08:20:00
