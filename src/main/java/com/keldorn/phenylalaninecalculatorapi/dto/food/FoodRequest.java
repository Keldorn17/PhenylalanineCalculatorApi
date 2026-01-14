package com.keldorn.phenylalaninecalculatorapi.dto.food;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record FoodRequest(@NotNull String name, @NotNull BigDecimal protein, @NotNull BigDecimal calories,
                          @NotNull Long foodTypeId) {}
