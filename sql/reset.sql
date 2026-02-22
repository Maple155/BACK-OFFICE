TRUNCATE TABLE Vehicules_Reservations CASCADE;
TRUNCATE TABLE Distances CASCADE;
TRUNCATE TABLE Reservation CASCADE;
TRUNCATE TABLE Vehicule CASCADE;
TRUNCATE TABLE TypeCarburant CASCADE;
TRUNCATE TABLE Lieu CASCADE;
TRUNCATE TABLE Parametres CASCADE;
TRUNCATE TABLE Tokens CASCADE;

ALTER SEQUENCE parametres_id_seq RESTART WITH 1;
ALTER SEQUENCE lieu_id_seq RESTART WITH 1;
ALTER SEQUENCE reservation_id_seq RESTART WITH 1;
ALTER SEQUENCE distances_id_seq RESTART WITH 1;
ALTER SEQUENCE typecarburant_id_seq RESTART WITH 1;
ALTER SEQUENCE vehicule_id_seq RESTART WITH 1;
ALTER SEQUENCE vehicules_reservations_id_seq RESTART WITH 1;
ALTER SEQUENCE tokens_id_seq RESTART WITH 1;

INSERT INTO Parametres (libelle, value, code, description) VALUES
('Vitesse moyen (km / h)', '40', 'VITESSE_MOYENNE_KMH', 'Vitesse moyenne utilisée pour estimer les trajets'),
('Temps d''attent (min)', '15', 'TEMPS_ATTENTE_MIN', 'Temps moyen d''attente avant prise en charge');

INSERT INTO Lieu (id, code, libelle) VALUES
(1, 'IVT', 'Ivato Aéroport'),
(2, 'TNR', 'Tanà Centre'),
(3, 'ANL', 'Analakely'),
(4, 'AMH', 'Ambohimanarina'),
(5, 'AMB', 'Ambatobe');

INSERT INTO Reservation (id, id_lieu, client, nbPassager, dateHeure) VALUES
(1, 3, '4631', 11, '2026-02-05 00:01'),
(2, 3, '4394', 1,  '2026-02-05 23:55'),
(3, 1, '8054', 2,  '2026-02-09 10:17'),
(4, 2, '1432', 4,  '2026-02-01 15:25'),
(5, 1, '7861', 4,  '2026-01-28 07:11'),
(6, 1, '3308', 5,  '2026-01-28 07:45'),
(7, 2, '4484', 13, '2026-02-28 08:25'),
(8, 2, '9687', 8,  '2026-02-28 13:00'),
(9, 1, '6302', 7,  '2026-02-15 13:00'),
(10, 4, '8640', 1, '2026-02-18 22:55');

INSERT INTO Distances (id_from, id_to, kilometer) VALUES
(1, 2, 16.50),
(1, 3, 17.20),
(2, 3, 2.80),
(2, 4, 8.10),
(3, 5, 9.75),
(4, 5, 6.90);

INSERT INTO TypeCarburant (id, libelle) VALUES
(1, 'Essence'),
(2, 'Diesel'),
(3, 'Electrique'),
(4, 'Hybride'),
(5, 'GPL');

INSERT INTO Vehicule (id, reference, nbPlaces, typeCarburant_id) VALUES
(1, 'VH-001', 4, 1),
(2, 'VH-002', 7, 2),
(3, 'VH-003', 5, 1),
(4, 'VH-004', 2, 3),
(5, 'VH-005', 8, 2),
(6, 'VH-006', 4, 4),
(7, 'VH-007', 5, 5),
(8, 'VH-008', 6, 1),
(9, 'VH-009', 4, 3),
(10, 'VH-010', 7, 2);

INSERT INTO Vehicules_Reservations (id_voiture, id_reservation) VALUES
(1, 1),
(2, 3),
(2, 4),
(3, 7),
(5, 8),
(8, 9);

INSERT INTO Tokens (dateExpiration) VALUES
('2024-01-01 00:00:00'),
('2024-06-15 12:30:00'),
('2024-12-31 23:59:59'),
('2026-12-31 23:59:59'),
('2027-06-30 18:00:00'),
('2028-01-01 00:00:00'),
(CURRENT_DATE + TIME '23:59:59'),
(NOW() + INTERVAL '1 hour'),
(NOW() + INTERVAL '7 days'),
('2030-01-01 00:00:00');

SELECT setval('lieu_id_seq', COALESCE((SELECT MAX(id) FROM Lieu), 1), true);
SELECT setval('reservation_id_seq', COALESCE((SELECT MAX(id) FROM Reservation), 1), true);
SELECT setval('typecarburant_id_seq', COALESCE((SELECT MAX(id) FROM TypeCarburant), 1), true);
SELECT setval('vehicule_id_seq', COALESCE((SELECT MAX(id) FROM Vehicule), 1), true);
SELECT setval('parametres_id_seq', COALESCE((SELECT MAX(id) FROM Parametres), 1), true);
SELECT setval('tokens_id_seq', COALESCE((SELECT MAX(id) FROM Tokens), 1), true);
SELECT setval('distances_id_seq', COALESCE((SELECT MAX(id) FROM Distances), 1), true);
SELECT setval('vehicules_reservations_id_seq', COALESCE((SELECT MAX(id) FROM Vehicules_Reservations), 1), true);

SELECT 'Parametres' as table_name, COUNT(*) as count FROM Parametres
UNION ALL
SELECT 'Lieux', COUNT(*) FROM Lieu
UNION ALL
SELECT 'Reservations', COUNT(*) FROM Reservation
UNION ALL
SELECT 'Distances', COUNT(*) FROM Distances
UNION ALL
SELECT 'Vehicules_Reservations', COUNT(*) FROM Vehicules_Reservations;