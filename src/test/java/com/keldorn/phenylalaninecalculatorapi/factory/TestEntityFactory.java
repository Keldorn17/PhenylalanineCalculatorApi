package com.keldorn.phenylalaninecalculatorapi.factory;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.*;
import com.keldorn.phenylalaninecalculatorapi.domain.enums.Role;

import java.math.BigDecimal;
import java.time.*;

public class TestEntityFactory {

    public static final String DEFAULT_USERNAME = "testUser";
    public static final String DEFAULT_EMAIL = "test@testmail.com";
    public static final String DEFAULT_PASSWORD = "testPassword";
    public static final String DEFAULT_TIMEZONE = "UTC";
    public static final String DEFAULT_FOOD_TYPE_NAME = "testFoodType";
    public static final String DEFAULT_FOOD_NAME = "TestFood";
    public static final BigDecimal DEFAULT_BIG_DECIMAL_VALUE = BigDecimal.TEN;
    public static final Integer DEFAULT_INTEGER_VALUE = 10;

    public static final LocalDate TEST_DATE = LocalDate.of(2026, 1, 1);
    public static final Instant START = ZonedDateTime.of(LocalDate.of(2026, 1, 1),
            LocalTime.of(0, 0), ZoneId.of("UTC")).toInstant();
    public static final Instant END = ZonedDateTime.of(LocalDate.of(2026, 1, 2),
            LocalTime.of(0, 0), ZoneId.of("UTC")).toInstant();
    public static final Instant CONSUMED_AT = ZonedDateTime.of(LocalDate.of(2026, 1, 1),
            LocalTime.of(12, 0), ZoneId.of("UTC")).toInstant();

    public static User user() {
        return User.builder()
                .timezone(DEFAULT_TIMEZONE)
                .dailyLimit(DEFAULT_BIG_DECIMAL_VALUE)
                .email(DEFAULT_EMAIL)
                .role(Role.ROLE_USER)
                .password(DEFAULT_PASSWORD)
                .username(DEFAULT_USERNAME)
                .build();
    }

    public static FoodType foodType() {
        return FoodType.builder()
                .name(DEFAULT_FOOD_TYPE_NAME)
                .multiplier(DEFAULT_INTEGER_VALUE)
                .build();
    }

    public static Food food(FoodType foodType) {
        return Food.builder()
                .name(DEFAULT_FOOD_NAME)
                .protein(DEFAULT_BIG_DECIMAL_VALUE)
                .phenylalanine(DEFAULT_BIG_DECIMAL_VALUE)
                .calories(DEFAULT_BIG_DECIMAL_VALUE)
                .foodType(foodType)
                .build();
    }

    public static FoodConsumption foodConsumption(User user, Food food, Instant consumedAt) {
        return FoodConsumption.builder()
                .user(user)
                .food(food)
                .amount(DEFAULT_BIG_DECIMAL_VALUE)
                .phenylalanineAmount(DEFAULT_BIG_DECIMAL_VALUE)
                .consumedAt(consumedAt)
                .build();
    }

    public static DailyIntake dailyIntake(User user, LocalDate date) {
        return DailyIntake.builder()
                .date(date)
                .user(user)
                .totalPhenylalanine(DEFAULT_BIG_DECIMAL_VALUE)
                .build();
    }
}
