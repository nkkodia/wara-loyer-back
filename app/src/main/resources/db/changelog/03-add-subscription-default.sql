-- Création d'une nouvelle version de migration pour Liquibase
-- Cette version ajoute une valeur par défaut à la colonne 'subscription_end_date'
-- dans la table 'app_user' pour éviter l'erreur de valeur nulle.

-- Un changeset unique pour cette mise à jour.
-- L'auteur et l'ID sont importants pour que Liquibase suive la migration.
-- ChangeSet id: add-default-to-subscription_end_date-app-user
-- Author: votre_nom_d_utilisateur
-- Date: 2025-08-20

-- Ajout d'une valeur par défaut à la colonne 'subscription_end_date'
-- La valeur par défaut est définie sur la date actuelle plus un an.
ALTER TABLE app_user
    ALTER COLUMN subscription_end_date SET DEFAULT NOW() + INTERVAL '1 year';
