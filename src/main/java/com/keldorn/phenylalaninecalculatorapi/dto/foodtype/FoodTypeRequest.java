package com.keldorn.phenylalaninecalculatorapi.dto.foodtype;

import jakarta.validation.constraints.NotNull;

public record FoodTypeRequest(@NotNull String name, @NotNull Integer multiplier) {}
