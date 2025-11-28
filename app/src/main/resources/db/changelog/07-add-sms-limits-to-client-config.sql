-- db/changelog/07-add-sms-limits-to-client-config.sql
-- ChangeSet id: add-sms-limits author: votre_nom_d_utilisateur date: 2025-09-15
ALTER TABLE client_config
    ADD COLUMN monthly_sms_limit INTEGER DEFAULT 100,
ADD COLUMN message_count_this_month INTEGER DEFAULT 0;
