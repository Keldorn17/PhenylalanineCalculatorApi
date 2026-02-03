package com.keldorn.phenylalaninecalculatorapi.factory;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.*;
import com.keldorn.phenylalaninecalculatorapi.domain.enums.Role;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class TestEntityFactory {

    public static final String DEFAULT_USERNAME = "testUser";
    public static final String DEFAULT_EMAIL = "test@testmail.com";
    public static final String DEFAULT_PASSWORD = "testPassword";
    public static final String DEFAULT_TIMEZONE = "UTC";
    public static final String DEFAULT_FOOD_TYPE_NAME = "testFoodType";
    public static final String DEFAULT_FOOD_NAME = "TestFood";

    public static User user() {
        return User.builder()
                .timezone(DEFAULT_TIMEZONE)
                .dailyLimit(BigDecimal.TEN)
                .email(DEFAULT_EMAIL)
                .role(Role.ROLE_USER)
                .password(DEFAULT_PASSWORD)
                .username(DEFAULT_USERNAME)
                .build();
    }

    public static FoodType foodType() {
        return FoodType.builder()
                .name(DEFAULT_FOOD_TYPE_NAME)
                .multiplier(10)
                .build();
    }

    public static Food food(FoodType foodType) {
        return Food.builder()
                .name(DEFAULT_FOOD_NAME)
                .protein(BigDecimal.TEN)
                .phenylalanine(BigDecimal.TEN)
                .calories(BigDecimal.TEN)
                .foodType(foodType)
                .build();
    }

    public static FoodConsumption foodConsumption(User user, Food food, Instant consumedAt) {
        return FoodConsumption.builder()
                .user(user)
                .food(food)
                .amount(BigDecimal.TEN)
                .phenylalanineAmount(BigDecimal.TEN)
                .consumedAt(consumedAt)
                .build();
    }

    public static DailyIntake dailyIntake(User user, LocalDate date) {
        return DailyIntake.builder()
                .date(date)
                .user(user)
                .totalPhenylalanine(BigDecimal.TEN)
                .build();
    }
}
