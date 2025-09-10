-- db/changelog/03-add-columns-to-sms-log.sql
-- ChangeSet id: add-columns-to-sms-log author: votre_nom_d_utilisateur date: 2025-09-10

-- Ajout de la colonne to_phone_number à la table sms_log
ALTER TABLE sms_log
    ADD COLUMN to_phone_number VARCHAR(50);

-- Modification du type de colonne pour sent_date de TIMESTAMP à DATE
ALTER TABLE sms_log
ALTER COLUMN sent_date TYPE DATE;
