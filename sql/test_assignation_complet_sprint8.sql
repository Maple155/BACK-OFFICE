-- =============================================================================
-- JEUX DE DONNÉES DE TEST — ASSIGNATION AUTOMATIQUE
-- Couvre : vagues, reliquats, véhicules de retour, backlog prioritaire, split
-- Date de test : 2026-03-19
-- =============================================================================

BEGIN;

-- =============================================================================
-- 0. NETTOYAGE
-- =============================================================================
TRUNCATE TABLE Vehicules_Reservations, Mission, Distances, Reservation,
               Vehicule, TypeCarburant, Lieu, Parametres CASCADE;

-- =============================================================================
-- 1. PARAMÈTRES SYSTÈME
-- =============================================================================
INSERT INTO Parametres (id, libelle, value, code, description) VALUES
(1, 'Vitesse moyenne (km/h)', '40',  'VITESSE_MOYENNE_KMH', 'Vitesse de croisière supposée'),
(2, 'Temps d''attente (min)',  '15',  'TEMPS_ATTENTE_MIN',   'Fenêtre de regroupement d''une vague');

-- =============================================================================
-- 2. LIEUX & DISTANCES
--    Lieu 1 = Aéroport (AEROPORT_ID = 1 dans le service)
-- =============================================================================
INSERT INTO Lieu (id, code, libelle) VALUES
(1, 'IVT', 'Ivato Aéroport'),
(2, 'TNR', 'Tanà Centre'),
(3, 'MAH', 'Mahamasina'),
(4, 'ALA', 'Alarobia'),
(5, 'IVA', 'Ivandry'),
(6, 'TAL', 'Talatamaty'),
(7, 'AMB', 'Ambohijanaka'),
(8, 'ANK', 'Ankadifotsy');

--  Distances depuis l'aéroport (id=1) vers chaque lieu
INSERT INTO Distances (id_from, id_to, kilometer) VALUES
(1, 2, 15.0),   -- Aéro → Tanà Centre  (≈ 22 min)
(1, 3, 17.0),   -- Aéro → Mahamasina   (≈ 25 min)
(1, 4, 10.0),   -- Aéro → Alarobia     (≈ 15 min)
(1, 5, 12.0),   -- Aéro → Ivandry      (≈ 18 min)
(1, 6,  4.5),   -- Aéro → Talatamaty   (≈  7 min)
(1, 7,  8.0),   -- Aéro → Ambohijanaka (≈ 12 min)
(1, 8, 20.0),   -- Aéro → Ankadifotsy  (≈ 30 min)
-- Distances inter-lieux (pour circuits multi-destinations)
(2, 3,  3.0),
(2, 4,  5.0),
(2, 5,  6.0),
(3, 4,  4.5),
(3, 8,  5.0),
(4, 5,  3.5),
(5, 6,  8.0),
(6, 7,  3.0),
(7, 8, 13.0);

-- =============================================================================
-- 3. FLOTTE
--    VH-001-MINI  :  4 places  — disponible dès 00h00 — Diesel
--    VH-002-MID   :  7 places  — disponible dès 00h00 — Essence
--    VH-003-MAX   :  9 places  — disponible dès 09h00 — Diesel
--    VH-004-XL    : 12 places  — disponible dès 00h00 — Diesel
--    VH-005-MAXI  : 15 places  — disponible dès 07h00 — Essence
-- =============================================================================
INSERT INTO TypeCarburant (id, libelle) VALUES
(1, 'Essence'),
(2, 'Diesel');

INSERT INTO Vehicule (id, reference, nbPlaces, heure_debut_disponibilite, typeCarburant_id) VALUES
(1, 'VH-001-MINI',  4,  '00:00:00', 2),
(2, 'VH-002-MID',   7,  '00:00:00', 1),
(3, 'VH-003-MAX',   9,  '09:00:00', 2),
(4, 'VH-004-XL',   12,  '00:00:00', 2),
(5, 'VH-005-MAXI', 15,  '07:00:00', 1);


-- =============================================================================
-- =============================================================================
-- SCÉNARIOS DE TEST
-- =============================================================================
-- =============================================================================


-- =============================================================================
-- SCÉNARIO A — VAGUE SIMPLE (06:00)
-- Objectif : Tous les passagers tiennent dans un seul véhicule existant.
--   R01 : 3 passagers → Talatamaty  (06:00)
--   R02 : 1 passager  → Ivandry     (06:10)
-- Attendu : VH-001-MINI (4 places) prend R01 + R02. Départ à 06:10 (dernière résa).
-- =============================================================================
INSERT INTO Reservation (id, id_lieu, client, nbPassager, dateHeure) VALUES
(1,  6, 'A-Client-Dupont',   3, '2026-03-19 06:00:00'),
(2,  5, 'A-Client-Martin',   1, '2026-03-19 06:10:00');


-- =============================================================================
-- SCÉNARIO B — VAGUE AVEC SPLIT (07:30)
-- Objectif : Tester la répartition quand aucun véhicule seul ne peut tout prendre.
--   R03 : 10 passagers → Tanà Centre   (07:30) — dépasse VH-002-MID (7 places)
--   R04 :  6 passagers → Alarobia      (07:35)
--   R05 :  3 passagers → Mahamasina    (07:42)
-- Attendu :
--   VH-004-XL (12 pl.) prend R03 entièrement + R05 en complément
--   VH-002-MID (7 pl.) prend R04 + reste éventuel
-- =============================================================================
INSERT INTO Reservation (id, id_lieu, client, nbPassager, dateHeure) VALUES
(3,  2, 'B-GroupeTanà',    10, '2026-03-19 07:30:00'),
(4,  4, 'B-GroupeAlarobia',  6, '2026-03-19 07:35:00'),
(5,  3, 'B-SoloMaha',        3, '2026-03-19 07:42:00');


-- =============================================================================
-- SCÉNARIO C — VAGUE MULTI-DESTINATIONS + RELIQUAT (09:00)
-- Objectif : Tester le reliquat quand la flotte est saturée.
--   R06 : 14 passagers → Ivandry       (09:00) — force reliquat
--   R07 :  8 passagers → Mahamasina    (09:05)
--   R08 :  5 passagers → Ambohijanaka  (09:12)
-- Attendu :
--   VH-005-MAXI (15 pl.) prend R06 (14) + 1 passager de R07
--   VH-003-MAX  (9 pl.)  prend les 7 restants de R07 + 2 passagers de R08
--   VH-004-XL   (12 pl.) prend les 3 restants de R08
--   Reliquat si flotte insuffisante → nouvelle ligne Reservation "(Reliquat)"
-- =============================================================================
INSERT INTO Reservation (id, id_lieu, client, nbPassager, dateHeure) VALUES
(6,  5, 'C-GrosGroupe-Ivandry',   14, '2026-03-19 09:00:00'),
(7,  3, 'C-GroupeMaha',            8, '2026-03-19 09:05:00'),
(8,  7, 'C-SoloAmbo',              5, '2026-03-19 09:12:00');


-- =============================================================================
-- SCÉNARIO D — VÉHICULE DE RETOUR CAS A (10:30)
-- Objectif : Tester fonctionnalité 1 CAS A — véhicule qui revient et trouve
--            une réservation >= sa capacité → départ IMMÉDIAT.
--
-- Pré-condition : VH-001-MINI (4 places) finit une mission à 10:35.
--   On simule sa mission précédente manuellement.
--   R09 : 5 passagers → Tanà Centre (10:30) — >= 4 places de VH-001-MINI
--
-- Attendu : VH-001-MINI crée une mission avec départ à 10:35 (son heure de retour),
--           prend 4 passagers de R09, crée un reliquat de 1 passager.
-- =============================================================================

-- Mission passée simulée pour VH-001-MINI (pour déclencher le "retour")
INSERT INTO Mission (id, id_vehicule, heure_arrivee_aero, heure_depart_prevu, heure_retour_prevu) VALUES
(1, 1, '2026-03-19 10:00:00', '2026-03-19 10:00:00', '2026-03-19 10:35:00');

-- Réservation sans assignation (non assignée = pas dans Vehicules_Reservations)
INSERT INTO Reservation (id, id_lieu, client, nbPassager, dateHeure) VALUES
(9,  2, 'D-RetourCasA-Tana',  5, '2026-03-19 10:30:00');


-- =============================================================================
-- SCÉNARIO E — VÉHICULE DE RETOUR CAS B + COMPLÉTION (10:30, même vague que D)
-- Objectif : Tester fonctionnalité 1 CAS B — véhicule de retour, aucune résa
--            >= sa capacité → attente + complétion avec petits groupes.
--
-- Pré-condition : VH-002-MID (7 places) finit une mission à 10:25.
--   R10 :  3 passagers → Alarobia     (10:28) — < 7 places
--   R11 :  2 passagers → Talatamaty   (10:32) — complément pendant l'attente
--   R12 :  2 passagers → Ivandry      (10:38) — complément (dans fenêtre attente)
--
-- Attendu :
--   VH-002-MID prend R10 (3) comme principale.
--   Attend jusqu'à max(10:25 + 15min = 10:40) ou véhicule plein.
--   Complète avec R11 (2) et R12 (2) → total 7/7 → départ à 10:38 (max dateHeure).
-- =============================================================================

-- Mission passée simulée pour VH-002-MID
INSERT INTO Mission (id, id_vehicule, heure_arrivee_aero, heure_depart_prevu, heure_retour_prevu) VALUES
(2, 2, '2026-03-19 09:55:00', '2026-03-19 09:55:00', '2026-03-19 10:25:00');

INSERT INTO Reservation (id, id_lieu, client, nbPassager, dateHeure) VALUES
(10, 4, 'E-RetourCasB-Principal',    3, '2026-03-19 10:28:00'),
(11, 6, 'E-RetourCasB-Complement1',  2, '2026-03-19 10:32:00'),
(12, 5, 'E-RetourCasB-Complement2',  2, '2026-03-19 10:38:00');


-- =============================================================================
-- SCÉNARIO F — BACKLOG PRIORITAIRE (11:00, Fonctionnalité 2)
-- Objectif : Tester que les réservations NON assignées des vagues passées
--            sont traitées en PREMIER dans la vague suivante.
--
-- On simule un backlog en insérant une réservation à 10:00 qui n'aurait
-- pas pu être traitée (ex: flotte saturée).
--   R13 : 4 passagers → Ankadifotsy   (10:00) — backlog non assigné
--   R14 : 6 passagers → Tanà Centre   (11:00) — nouvelle vague
--   R15 : 3 passagers → Alarobia      (11:08)
--
-- Attendu :
--   R13 (backlog) est traité EN PREMIER dans la vague 11:00.
--   VH-001-MINI (4 pl.) prend R13 immédiatement (backlog prioritaire).
--   VH-002-MID (7 pl.) prend R14 + R15.
-- =============================================================================
INSERT INTO Reservation (id, id_lieu, client, nbPassager, dateHeure) VALUES
(13, 8, 'F-Backlog-Ancien',       4, '2026-03-19 10:00:00'),  -- backlog
(14, 2, 'F-NouvelleVague-Tana',   6, '2026-03-19 11:00:00'),
(15, 4, 'F-NouvelleVague-Alar',   3, '2026-03-19 11:08:00');


-- =============================================================================
-- SCÉNARIO G — VÉHICULE AVEC HEURE DÉBUT DISPONIBILITÉ (09:00)
-- Objectif : Tester que VH-003-MAX (dispo à partir de 09h00) ne peut pas
--            être sélectionné pour une vague antérieure (ex: 07h30).
--   → Le scénario B à 07:30 ne doit PAS utiliser VH-003-MAX.
--   → Le scénario C à 09:00 PEUT l'utiliser.
-- (Pas de réservation supplémentaire — couvert par scénarios B et C)
-- =============================================================================
-- Aucune insertion supplémentaire nécessaire.


-- =============================================================================
-- SCÉNARIO H — VAGUE TARDIVE AVEC CAPACITÉ EXACTE (14:00)
-- Objectif : Tester le cas "fit parfait" où un véhicule a exactement
--            le bon nombre de places pour une réservation.
--   R16 :  9 passagers → Mahamasina   (14:00) — exactement VH-003-MAX (9 pl.)
--   R17 :  7 passagers → Ivandry      (14:05) — exactement VH-002-MID (7 pl.)
--   R18 :  4 passagers → Talatamaty   (14:10) — exactement VH-001-MINI (4 pl.)
--
-- Attendu :
--   Chaque véhicule prend exactement sa réservation. Aucun reliquat.
--   Départ synchronisé à 14:10 (dernière dateHeure de la vague).
-- =============================================================================
INSERT INTO Reservation (id, id_lieu, client, nbPassager, dateHeure) VALUES
(16, 3, 'H-FitParfait-Max',   9, '2026-03-19 14:00:00'),
(17, 5, 'H-FitParfait-Mid',   7, '2026-03-19 14:05:00'),
(18, 6, 'H-FitParfait-Mini',  4, '2026-03-19 14:10:00');


-- =============================================================================
-- SCÉNARIO I — VAGUE AVEC SOLO (16:00)
-- Objectif : Tester le cas minimal — un seul passager seul dans un véhicule.
--   R19 :  1 passager → Ambohijanaka  (16:00)
--
-- Attendu :
--   VH-001-MINI (4 pl.) prend R19. 3 places restent libres (non utilisées).
--   Mission créée normalement.
-- =============================================================================
INSERT INTO Reservation (id, id_lieu, client, nbPassager, dateHeure) VALUES
(19, 7, 'I-Solo-Ambo', 1, '2026-03-19 16:00:00');


-- =============================================================================
-- SCÉNARIO J — GRANDE VAGUE SATURANT TOUTE LA FLOTTE (18:00)
-- Objectif : Tester le reliquat global quand TOUS les véhicules sont pris
--            et qu'il reste encore des passagers non assignés.
--   Total flotte disponible à 18:00 = 4+7+9+12+15 = 47 places
--   Total passagers = 55 → 8 passagers en reliquat
--
--   R20 : 20 passagers → Tanà Centre   (18:00)
--   R21 : 15 passagers → Mahamasina    (18:03)
--   R22 : 12 passagers → Ivandry       (18:07)
--   R23 :  8 passagers → Alarobia      (18:12)
-- =============================================================================
INSERT INTO Reservation (id, id_lieu, client, nbPassager, dateHeure) VALUES
(20, 2, 'J-Saturation-Tana',   20, '2026-03-19 18:00:00'),
(21, 3, 'J-Saturation-Maha',   15, '2026-03-19 18:03:00'),
(22, 5, 'J-Saturation-Ivan',   12, '2026-03-19 18:07:00'),
(23, 4, 'J-Saturation-Alar',    8, '2026-03-19 18:12:00');


-- =============================================================================
-- RÉCAPITULATIF DES SCÉNARIOS
-- =============================================================================
/*
  Scénario  Heure    Réservations   Véhicules impliqués          Fonctionnalité testée
  --------  -------  -------------  ---------------------------  ---------------------------
  A         06:00    R01-R02        VH-001-MINI                  Vague simple, fit exact
  B         07:30    R03-R05        VH-002-MID, VH-004-XL        Split multi-véhicules
  C         09:00    R06-R08        VH-003-MAX, VH-004-XL,       Reliquat, saturation partielle
                                    VH-005-MAXI
  D         10:30    R09            VH-001-MINI (de retour)       Véhicule retour CAS A (départ immédiat)
  E         10:30    R10-R12        VH-002-MID (de retour)        Véhicule retour CAS B (attente + complétion)
  F         11:00    R13(backlog),  VH-001-MINI, VH-002-MID      Backlog prioritaire (Fonctionnalité 2)
                     R14-R15
  G         07:30/   (couvert par B et C)                         Heure début disponibilité véhicule
             09:00
  H         14:00    R16-R18        VH-001-MINI, VH-002-MID,     Fit parfait, départ synchronisé
                                    VH-003-MAX
  I         16:00    R19            VH-001-MINI                  Cas solo (1 passager)
  J         18:00    R20-R23        Toute la flotte              Saturation totale + reliquat global
*/

-- =============================================================================
-- MISE À JOUR DES SÉQUENCES
-- =============================================================================
SELECT setval('reservation_id_seq', (SELECT MAX(id) FROM Reservation));
SELECT setval('mission_id_seq',     (SELECT MAX(id) FROM Mission));

COMMIT;
