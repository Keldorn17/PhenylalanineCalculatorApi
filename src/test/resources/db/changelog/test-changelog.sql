-- liquibase formatted sql

-- Test User
-- changeset Patai Zoltán:load-test-user
-- Loads test user
INSERT INTO users (username, email, password, role, daily_limit, timezone)
VALUES ('testUser', 'test@testmail.com', '$2a$10$FzMGv14lGX0uJSr5DCAxQu9k/6/2yoaLw9eP59snqmbCiePoD3Gti', 'ROLE_USER',
        400, 'UTC');
