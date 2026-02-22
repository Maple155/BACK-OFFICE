CREATE TABLE Parametres (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(255) NOT NULL,
    value VARCHAR(255) NOT NULL,
    code VARCHAR(100),
    description VARCHAR(500)
);

CREATE TABLE Lieu (
    id SERIAL PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    libelle VARCHAR(255) NOT NULL
);

CREATE TABLE Reservation (
    id SERIAL PRIMARY KEY,
    id_lieu INT NOT NULL,
    client VARCHAR(100) NOT NULL,
    nbPassager INT NOT NULL,
    dateHeure TIMESTAMP NOT NULL,

    CONSTRAINT fk_reservation_lieu
        FOREIGN KEY (id_lieu)
        REFERENCES Lieu(id)
        ON DELETE CASCADE
);

CREATE TABLE Distances (
    id SERIAL PRIMARY KEY,
    id_from INT NOT NULL,
    id_to INT NOT NULL,
    kilometer NUMERIC(10, 2) NOT NULL,

    CONSTRAINT fk_distance_from
        FOREIGN KEY (id_from)
        REFERENCES Lieu(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_distance_to
        FOREIGN KEY (id_to)
        REFERENCES Lieu(id)
        ON DELETE CASCADE,
    CONSTRAINT chk_distance_positive
        CHECK (kilometer >= 0)
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

CREATE TABLE Vehicules_Reservations (
    id SERIAL PRIMARY KEY,
    id_voiture INT NOT NULL,
    id_reservation INT NOT NULL,

    CONSTRAINT fk_vehicule_reservation_vehicule
        FOREIGN KEY (id_voiture)
        REFERENCES Vehicule(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_vehicule_reservation_reservation
        FOREIGN KEY (id_reservation)
        REFERENCES Reservation(id)
        ON DELETE CASCADE,
    CONSTRAINT uq_vehicule_reservation UNIQUE (id_voiture, id_reservation)
);

CREATE TABLE Tokens (
    id SERIAL PRIMARY KEY,
    token UUID NOT NULL DEFAULT gen_random_uuid(),
    dateExpiration TIMESTAMP NOT NULL
);

CREATE INDEX idx_reservation_dateHeure ON Reservation(dateHeure);
CREATE INDEX idx_reservation_client ON Reservation(client);
CREATE INDEX idx_reservation_lieu ON Reservation(id_lieu);
CREATE INDEX idx_distances_from ON Distances(id_from);
CREATE INDEX idx_distances_to ON Distances(id_to);
CREATE INDEX idx_vehicules_reservations_voiture ON Vehicules_Reservations(id_voiture);
CREATE INDEX idx_vehicules_reservations_reservation ON Vehicules_Reservations(id_reservation);

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