package com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FoodConsumptionResponse(Long id, BigDecimal amount, BigDecimal phenylalanineAmount, LocalDateTime consumedAt) {}
