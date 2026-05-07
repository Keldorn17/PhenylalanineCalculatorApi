package com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;

import lombok.Builder;

@Builder
public record FoodConsumptionRequest(@NotNull BigDecimal amount) {}
