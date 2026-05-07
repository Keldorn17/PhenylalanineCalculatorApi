package com.keldorn.phenylalaninecalculatorapi.dto.food;

import java.math.BigDecimal;

import jakarta.validation.constraints.PositiveOrZero;

import lombok.Builder;

@Builder
public record FoodUpdateRequest(String name, @PositiveOrZero BigDecimal protein, @PositiveOrZero BigDecimal calories,
                                @PositiveOrZero Long foodTypeId) {}
