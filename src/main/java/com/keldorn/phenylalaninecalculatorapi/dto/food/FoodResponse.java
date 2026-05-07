package com.keldorn.phenylalaninecalculatorapi.dto.food;

import java.math.BigDecimal;

import lombok.Builder;

@Builder
public record FoodResponse(Long id, String name, BigDecimal protein, BigDecimal calories,
                           BigDecimal phenylalanine, String foodTypeName, Integer multiplier, boolean canEdit) {}
