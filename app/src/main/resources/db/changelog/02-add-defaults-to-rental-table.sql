-- Liquibase ChangeSet pour corriger la table 'rental'
-- Ce script ajoute des valeurs par défaut pour éviter les erreurs de NOT NULL.

-- ChangeSet id: add-defaults-to-rental-table
-- Author: votre_nom_d_utilisateur
-- Date: 2025-08-25

-- Ajout d'une valeur par défaut pour la colonne is_reminder_sent
ALTER TABLE rental
    ALTER COLUMN is_reminder_sent SET DEFAULT FALSE;

-- Ajout d'une valeur par défaut pour la colonne is_relance_sent
ALTER TABLE rental
    ALTER COLUMN is_relance_sent SET DEFAULT FALSE;

