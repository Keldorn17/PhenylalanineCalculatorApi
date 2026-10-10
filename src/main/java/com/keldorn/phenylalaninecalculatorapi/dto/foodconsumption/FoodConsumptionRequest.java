package com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.Builder;

@Builder
public record FoodConsumptionRequest(
        @Positive @NotNull BigDecimal amount,
        ZonedDateTime consumedAt
) {

    public FoodConsumptionRequest(BigDecimal amount) {
        this(amount, null);
    }

}
