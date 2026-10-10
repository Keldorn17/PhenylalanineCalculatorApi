package com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import lombok.Builder;

@Builder
public record FoodConsumptionCreateRequest(
        @NotNull @PositiveOrZero Long foodId,
        @Positive @NotNull BigDecimal amount,
        ZonedDateTime consumedAt
) {

    public FoodConsumptionCreateRequest(Long foodId, BigDecimal amount) {
        this(foodId, amount, null);
    }

}
