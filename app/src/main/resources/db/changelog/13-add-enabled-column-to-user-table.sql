-- db/changelog/13-add-enabled-column-to-user-table.sql
ALTER TABLE app_user ADD COLUMN enabled BOOLEAN NOT NULL DEFAULT FALSE;