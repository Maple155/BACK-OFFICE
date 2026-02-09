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