-- liquibase formatted sql

-- Test User
-- changeset Patai Zoltán:load-test-user
-- Loads test user
INSERT INTO users (username, email, password, role, daily_limit)
VALUES ('testUser', 'test@testmail.com', '$2a$10$FzMGv14lGX0uJSr5DCAxQu9k/6/2yoaLw9eP59snqmbCiePoD3Gti', 'ROLE_USER',
        400);

-- Test Food Type
-- changeset Patai Zoltán:load-test-food-type
-- Loads food type
INSERT INTO food_type (name, multiplier, is_deleted)
VALUES ('testFoodType', 10, false);
INSERT INTO food_type (name, multiplier, is_deleted)
VALUES ('testFoodType', 10, false);
INSERT INTO food_type (name, multiplier, is_deleted)
VALUES ('testFoodType', 10, true);

-- Test Food
-- changeset Patai Zoltán:load-test-food
-- Loads food
INSERT INTO food (name, protein, calories, user_id, food_type_id, phenylalanine)
VALUES ('testFood', 10, 10, 1, 1, 10);
INSERT INTO food (name, protein, calories, user_id, food_type_id, phenylalanine)
VALUES ('apple', .26, 52, 1, 1, 2.60);

-- Test Food Consumption
-- changeset Patai Zoltán:load-test-food-consumption
-- Loads food consumption
INSERT INTO food_consumption (amount, phenylalanine_amount, consumed_at, food_id, user_id)
VALUES (10, 10, '2026-01-01T00:00', 1, 1);

-- Test Daily Intake
-- changeset Patai Zoltán:load-test-daily-intake
-- Loads daily intake
INSERT INTO daily_intake (date, total_phenylalanine, user_id)
VALUES ('2026-01-01', 10, 1)