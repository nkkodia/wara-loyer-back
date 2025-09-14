-- db/changelog/06-add-rental-id-to-tenant-message-log.sql
-- ChangeSet id: add-rental-id-to-tenant-message-log author: votre_nom_d_utilisateur date: 2025-09-14

-- Ajout de la colonne rental_id à la table tenant_message_log
ALTER TABLE tenant_message_log
    ADD COLUMN rental_id BIGINT;

-- Ajout de la clé étrangère pour la table 'rental'
ALTER TABLE tenant_message_log
    ADD CONSTRAINT fk_tenant_message_log_rental
        FOREIGN KEY (rental_id) REFERENCES rental(id);