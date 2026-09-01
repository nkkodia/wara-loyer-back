-- db/changelog/11-add-price-column-to-subscription.sql
ALTER TABLE subscription
    ADD COLUMN price DECIMAL(19, 2) NOT NULL DEFAULT 0;