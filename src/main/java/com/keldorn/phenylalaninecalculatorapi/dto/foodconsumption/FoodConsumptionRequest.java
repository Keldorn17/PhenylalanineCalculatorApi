package com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record FoodConsumptionRequest(@NotNull BigDecimal amount) {}
