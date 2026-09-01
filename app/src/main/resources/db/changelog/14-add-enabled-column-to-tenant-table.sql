-- db/changelog/14-add-enabled-column-to-tenant-table.sql
ALTER TABLE tenant ADD COLUMN enabled BOOLEAN NOT NULL DEFAULT TRUE;