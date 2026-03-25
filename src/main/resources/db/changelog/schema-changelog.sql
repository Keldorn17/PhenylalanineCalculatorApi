-- liquibase formatted sql

-- changeset Patai Zoltan:create-users-table
-- Creates Users table
-- rollback DROP TABLE users
CREATE TABLE users
(
    user_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(255) UNIQUE NOT NULL,
    email       VARCHAR(255) UNIQUE NOT NULL,
    password    VARCHAR(255)        NOT NULL,
    role        VARCHAR(255)        NOT NULL,
    daily_limit DECIMAL(38, 2),
    timezone    VARCHAR(255)        NOT NULL
);

-- changeset Patai Zoltan:create-food_type-table
-- Creates Food Type table
-- rollback DROP TABLE food_type
CREATE TABLE food_type
(
    food_type_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    multiplier   INT          NOT NULL
);

-- changeset Patai Zoltan:create-food-table
-- Creates Food table
-- rollback DROP TABLE food
CREATE TABLE food
(
    food_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(255)   NOT NULL,
    protein       DECIMAL(10, 2) NOT NULL,
    calories      DECIMAL(10, 2) NOT NULL,
    user_id       BIGINT,
    food_type_id  BIGINT         NOT NULL,
    phenylalanine DECIMAL(10, 2),
    FOREIGN KEY (user_id) REFERENCES users (user_id),
    FOREIGN KEY (food_type_id) REFERENCES food_type (food_type_id)
);

-- changeset Patai Zoltan:create-daily_intake-table
-- Creates Daily Intake table
-- rollback DROP TABLE daily_intake
CREATE TABLE daily_intake
(
    daily_intake_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    date                DATE,
    total_phenylalanine DECIMAL(12, 4),
    user_id             BIGINT,
    FOREIGN KEY (user_id) REFERENCES users (user_id),
    UNIQUE INDEX uq_daily_intake_user_date (user_id, date)
);

-- changeset Patai Zoltan:create-food_consumption-table
-- Creates Food Consumption table
-- rollback DROP TABLE food_consumption
CREATE TABLE food_consumption
(
    food_consumption_id  BIGINT AUTO_INCREMENT PRIMARY KEY,
    amount               DECIMAL(10, 2),
    phenylalanine_amount DECIMAL(12, 4),
    consumed_at          DATETIME(6),
    food_id              BIGINT,
    user_id              BIGINT,
    FOREIGN KEY (food_id) REFERENCES food (food_id),
    FOREIGN KEY (user_id) REFERENCES users (user_id)
);