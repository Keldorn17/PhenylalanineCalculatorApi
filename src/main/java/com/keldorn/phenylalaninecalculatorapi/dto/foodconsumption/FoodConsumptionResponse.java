package com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record FoodConsumptionResponse(Long id, String foodName, BigDecimal amount, BigDecimal phenylalanineAmount,
                                      LocalDateTime consumedAt) {}
