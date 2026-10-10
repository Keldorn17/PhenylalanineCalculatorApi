package com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import lombok.Builder;

@Builder
public record FoodConsumptionResponse(
        Long id,
        Long foodId,
        String foodName,
        BigDecimal amount,
        BigDecimal phenylalanineAmount,
        ZonedDateTime consumedAt
) {}
