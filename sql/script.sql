-- [ Suppression des tables existantes ]
DROP TABLE IF EXISTS Vehicules_Reservations CASCADE;
DROP TABLE IF EXISTS Mission CASCADE;
DROP TABLE IF EXISTS Vehicule CASCADE;
DROP TABLE IF EXISTS TypeCarburant CASCADE;
DROP TABLE IF EXISTS Distances CASCADE;
DROP TABLE IF EXISTS Reservation CASCADE;
DROP TABLE IF EXISTS Lieu CASCADE;
DROP TABLE IF EXISTS Parametres CASCADE;
DROP TABLE IF EXISTS Tokens CASCADE;

-- 1. Paramètres Système
CREATE TABLE Parametres (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(255) NOT NULL,
    value VARCHAR(255) NOT NULL,
    code VARCHAR(100) UNIQUE,
    description VARCHAR(500)
);

-- 2. Lieux et Distances
CREATE TABLE Lieu (
    id SERIAL PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    libelle VARCHAR(255) NOT NULL
);

CREATE TABLE Distances (
    id SERIAL PRIMARY KEY,
    id_from INT NOT NULL REFERENCES Lieu(id) ON DELETE CASCADE,
    id_to INT NOT NULL REFERENCES Lieu(id) ON DELETE CASCADE,
    kilometer NUMERIC(10, 2) NOT NULL CHECK (kilometer >= 0)
);

-- 3. Réservations
CREATE TABLE Reservation (
    id SERIAL PRIMARY KEY,
    id_lieu INT NOT NULL REFERENCES Lieu(id) ON DELETE CASCADE,
    client VARCHAR(100) NOT NULL,
    nbPassager INT NOT NULL,
    dateHeure TIMESTAMP NOT NULL
);

-- 4. Flotte de Véhicules
CREATE TABLE TypeCarburant (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(255) NOT NULL
);

CREATE TABLE Vehicule (
    id SERIAL PRIMARY KEY,
    reference VARCHAR(255) NOT NULL,
    nbPlaces INT NOT NULL,
    typeCarburant_id INT NOT NULL REFERENCES TypeCarburant(id) ON DELETE CASCADE
);

-- 5. Missions (Logique temporelle pure)
CREATE TABLE Mission (
    id SERIAL PRIMARY KEY,
    id_vehicule INT NOT NULL REFERENCES Vehicule(id) ON DELETE CASCADE,
    heure_arrivee_aero TIMESTAMP NOT NULL, -- Arrivée au terminal
    heure_depart_prevu TIMESTAMP NOT NULL,  -- Départ vers les clients
    heure_retour_prevu TIMESTAMP            -- Retour prévu à l'aéroport (Calculé)
);

-- 6. Liaison
CREATE TABLE Vehicules_Reservations (
    id_mission INT NOT NULL REFERENCES Mission(id) ON DELETE CASCADE,
    id_reservation INT NOT NULL REFERENCES Reservation(id) ON DELETE CASCADE,
    PRIMARY KEY (id_mission, id_reservation)
);

-- 7. Tokens
CREATE TABLE Tokens (
    id SERIAL PRIMARY KEY,
    token UUID NOT NULL DEFAULT gen_random_uuid(),
    dateExpiration TIMESTAMP NOT NULL
);

-- Index pour la performance (Statut supprimé ici)
CREATE INDEX idx_reservation_dateHeure ON Reservation(dateHeure);
CREATE INDEX idx_mission_depart ON Mission(heure_depart_prevu);
CREATE INDEX idx_mission_retour ON Mission(heure_retour_prevu);