-- db/changelog/15-create-password-reset-token-table.sql

-- Changement pour créer la table de jetons de réinitialisation de mot de passe
-- Utilisé par la fonctionnalité "Mot de passe oublié"
CREATE TABLE password_reset_token (
                                      id BIGSERIAL PRIMARY KEY,

    -- Le jeton lui-même, doit être unique et non nul
                                      token VARCHAR(255) NOT NULL UNIQUE,

    -- Date d'expiration du jeton (par exemple, 1 heure)
                                      expiry_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,

    -- Lien vers l'utilisateur (clé étrangère)
                                      user_id BIGINT NOT NULL,

    -- Contrainte de clé étrangère vers la table app_user
                                      CONSTRAINT fk_token_user_id
                                          FOREIGN KEY (user_id)
                                              REFERENCES app_user (id)
                                              ON DELETE CASCADE
);

-- Index pour accélérer la recherche par jeton
CREATE INDEX idx_prt_token ON password_reset_token (token);