-- db/changelog/10-add-subscription-to-user.sql
ALTER TABLE app_user
    ADD COLUMN subscription_id BIGINT;

ALTER TABLE app_user
    ADD CONSTRAINT fk_user_subscription FOREIGN KEY (subscription_id) REFERENCES subscription(id);