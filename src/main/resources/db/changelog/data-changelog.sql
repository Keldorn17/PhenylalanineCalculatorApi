-- liquibase formatted sql

-- changeset Patai Zoltan:add-role-user
-- Adds ROLE-USER to Role
-- rollback DELETE FROM roles;
INSERT IGNORE INTO roles (name) VALUES ('ROLE_USER');