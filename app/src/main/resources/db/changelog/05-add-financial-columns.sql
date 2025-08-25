-- Liquibase ChangeSet pour ajouter les colonnes de gestion financière à la table 'rental'.

-- ChangeSet id: add-financial-columns-to-rental
-- Author: votre_nom_d_utilisateur
-- Date: 2025-08-23

-- Ajout de la colonne pour les coûts mensuels imprévus
ALTER TABLE rental
    ADD COLUMN monthly_costs DECIMAL(19, 2);

-- Ajout de la colonne pour les impôts (12% du loyer hors charges)
ALTER TABLE rental
    ADD COLUMN taxes DECIMAL(19, 2);
