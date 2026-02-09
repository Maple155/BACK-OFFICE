-- 1. Désactiver temporairement les contraintes de clé étrangère (si nécessaire)
-- SET session_replication_role = 'replica'; -- Pour PostgreSQL

-- 2. Supprimer toutes les données des tables (dans l'ordre inverse des dépendances)
TRUNCATE TABLE Reservation CASCADE;
TRUNCATE TABLE Hotel CASCADE;

-- 3. Réactiver les contraintes de clé étrangère
-- SET session_replication_role = 'origin';

-- 4. Réinitialiser les séquences (auto-incréments)
ALTER SEQUENCE hotel_id_seq RESTART WITH 1;
ALTER SEQUENCE reservation_id_seq RESTART WITH 1;

INSERT INTO Hotel (nom) VALUES
('Colbert'),
('Novotel'),
('Ibis'),
('Lokanga');

INSERT INTO Reservation (id, id_hotel, client, nbPassager, dateHeure) VALUES
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

-- 6. Vérification
SELECT 'Hôtels :' as table_name, COUNT(*) as count FROM Hotel
UNION ALL
SELECT 'Réservations :' as table_name, COUNT(*) as count FROM Reservation;