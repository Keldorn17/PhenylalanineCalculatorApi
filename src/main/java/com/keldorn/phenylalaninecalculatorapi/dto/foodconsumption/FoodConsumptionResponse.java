package com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption;

import java.math.BigDecimal;
import java.sql.Timestamp;

public record FoodConsumptionResponse(Long id, BigDecimal amount, Timestamp consumedAt, BigDecimal phenylalanineAmount) {}
