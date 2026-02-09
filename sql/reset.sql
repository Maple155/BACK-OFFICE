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

-- 5. Réinsérer les données d'exemple
INSERT INTO Hotel (nom) VALUES
('Hotel Paris'),
('Hotel London'),
('Hotel New York');

INSERT INTO Reservation (id_hotel, client, nbPassager, dateHeure) VALUES
(1, 'CLIENT-001', 2, '2024-01-15 14:30:00'),
(2, 'CLIENT-002', 4, '2024-01-16 10:00:00'),
(1, 'CLIENT-003', 1, '2024-01-17 16:45:00'),
(3, 'CLIENT-004', 3, '2024-01-18 09:15:00'),
(2, 'CLIENT-005', 2, '2024-01-19 18:30:00');

-- 6. Vérification
SELECT 'Hôtels :' as table_name, COUNT(*) as count FROM Hotel
UNION ALL
SELECT 'Réservations :' as table_name, COUNT(*) as count FROM Reservation;