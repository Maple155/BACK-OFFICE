-- Création de la table Hotel
CREATE TABLE Hotel (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(255) NOT NULL
);

-- Création de la table Reservation
CREATE TABLE Reservation (
    id SERIAL PRIMARY KEY,
    id_hotel INT NOT NULL,
    client VARCHAR(100) NOT NULL,
    nbPassager INT NOT NULL,
    dateHeure TIMESTAMP NOT NULL,
    
    CONSTRAINT fk_hotel 
        FOREIGN KEY (id_hotel) 
        REFERENCES Hotel(id)
        ON DELETE CASCADE
);

CREATE TABLE TypeCarburant (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(255) NOT NULL
);

CREATE TABLE Vehicule (
    id SERIAL PRIMARY KEY,
    reference VARCHAR(255) NOT NULL,
    nbPlaces INT NOT NULL,
    typeCarburant_id INT NOT NULL,

    CONSTRAINT fk_typeCarburant 
        FOREIGN KEY (typeCarburant_id) 
        REFERENCES TypeCarburant(id)
        ON DELETE CASCADE
);

CREATE TABLE Tokens (
    id SERIAL PRIMARY KEY,
    token UUID NOT NULL DEFAULT gen_random_uuid(),
    dateExpiration TIMESTAMP NOT NULL
);

CREATE INDEX idx_reservation_dateHeure ON Reservation(dateHeure);
CREATE INDEX idx_reservation_client ON Reservation(client);
CREATE INDEX idx_reservation_hotel ON Reservation(id_hotel);

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

-- Insérer des données de test pour TypeCarburant
INSERT INTO TypeCarburant (id, libelle) VALUES
(1, 'Essence'),
(2, 'Diesel'),
(3, 'Electrique'),
(4, 'Hybride'),
(5, 'GPL');

-- Insérer des données de test pour Vehicule
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

-- Sans ID spécifique (laisser SERIAL faire son travail)
INSERT INTO Tokens (dateExpiration) VALUES
-- Tokens expirés
('2024-01-01 00:00:00'),
('2024-06-15 12:30:00'),
('2024-12-31 23:59:59'),

-- Tokens non expirés
('2026-12-31 23:59:59'),
('2027-06-30 18:00:00'),
('2028-01-01 00:00:00'),

-- Tokens avec dates relatives
(CURRENT_DATE + TIME '23:59:59'),
(NOW() + INTERVAL '1 hour'),
(NOW() + INTERVAL '7 days'),
('2030-01-01 00:00:00');