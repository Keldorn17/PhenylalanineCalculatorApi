package com.keldorn.phenylalaninecalculatorapi.dto.food;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import lombok.Builder;

@Builder
public record FoodRequest(@NotBlank String name, @NotNull @PositiveOrZero BigDecimal protein,
                          @NotNull @PositiveOrZero BigDecimal calories, @NotNull @PositiveOrZero Long foodTypeId) {}
