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

-- Option : Insertion de données d'exemple
INSERT INTO Hotel (nom) VALUES
('Hotel Paris'),
('Hotel London'),
('Hotel New York');

INSERT INTO Reservation (id_hotel, client, nbPassager, dateHeure) VALUES
(1, 'CLIENT-001', 2, '2024-01-15 14:30:00'),
(2, 'CLIENT-002', 4, '2024-01-16 10:00:00'),
(1, 'CLIENT-003', 1, '2024-01-17 16:45:00');