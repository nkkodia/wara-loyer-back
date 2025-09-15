-- db/changelog/09-update-subscriptions.sql
-- ChangeSet id: update-subscription-plans author: votre_nom_d_utilisateur date: 2025-09-15

-- Suppression des anciens abonnements
DELETE FROM subscription;

-- Insertion des nouveaux plans
INSERT INTO subscription (name, monthly_sms_limit, price) VALUES ('BASIC', 15, 10000);
INSERT INTO subscription (name, monthly_sms_limit, price) VALUES ('STANDARD', 150, 25000);
INSERT INTO subscription (name, monthly_sms_limit, price) VALUES ('PREMIUM', 300, 45000);
INSERT INTO subscription (name, monthly_sms_limit, price) VALUES ('PRO', 600, 75000);