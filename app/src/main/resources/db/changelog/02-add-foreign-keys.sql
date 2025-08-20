-- Ajout des clés étrangères sur la table 'client_config'
ALTER TABLE client_config
    ADD CONSTRAINT fk_client_config_user
        FOREIGN KEY (user_id) REFERENCES app_user(id);

-- Ajout des clés étrangères sur la table 'property'
ALTER TABLE property
    ADD CONSTRAINT fk_property_user
        FOREIGN KEY (user_id) REFERENCES app_user(id);

-- Ajout des clés étrangères sur la table 'tenant'
ALTER TABLE tenant
    ADD CONSTRAINT fk_tenant_user
        FOREIGN KEY (user_id) REFERENCES app_user(id);

-- Ajout de la clé étrangère pour associer un locataire à un bien
ALTER TABLE tenant
    ADD CONSTRAINT fk_tenant_property
        FOREIGN KEY (property_id) REFERENCES property(id);

-- Ajout des clés étrangères sur la table 'rental'
ALTER TABLE rental
    ADD CONSTRAINT fk_rental_user
        FOREIGN KEY (user_id) REFERENCES app_user(id);

ALTER TABLE rental
    ADD CONSTRAINT fk_rental_property
        FOREIGN KEY (property_id) REFERENCES property(id);

ALTER TABLE rental
    ADD CONSTRAINT fk_rental_locataire
        FOREIGN KEY (locataire_id) REFERENCES tenant(id);

-- Ajout des clés étrangères sur la table 'sms_log'
ALTER TABLE sms_log
    ADD CONSTRAINT fk_sms_log_user
        FOREIGN KEY (user_id) REFERENCES app_user(id);

ALTER TABLE sms_log
    ADD CONSTRAINT fk_sms_log_rental
        FOREIGN KEY (rental_id) REFERENCES rental(id);

ALTER TABLE sms_log
    ADD CONSTRAINT fk_sms_log_locataire
        FOREIGN KEY (locataire_id) REFERENCES tenant(id);

ALTER TABLE sms_log
    ADD CONSTRAINT fk_sms_log_property
        FOREIGN KEY (property_id) REFERENCES property(id);
