-- db/changelog/15-add-property-limit-to-subscription.sql

-- ChangeSet id: add-property-limit author: votre_nom_d_utilisateur date: 2025-11-28
ALTER TABLE subscription
    ADD COLUMN max_properties_limit INTEGER DEFAULT 5;