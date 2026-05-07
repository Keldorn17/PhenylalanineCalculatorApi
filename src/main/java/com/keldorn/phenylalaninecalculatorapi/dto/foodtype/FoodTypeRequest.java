package com.keldorn.phenylalaninecalculatorapi.dto.foodtype;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import lombok.Builder;

@Builder
public record FoodTypeRequest(@NotBlank String name, @NotNull @PositiveOrZero Integer multiplier) {}
