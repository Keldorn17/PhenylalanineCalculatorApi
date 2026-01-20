package com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption;

import java.math.BigDecimal;

public record FoodConsumptionResponse(Long id, BigDecimal amount, BigDecimal phenylalanineAmount) {}
