package com.keldorn.phenylalaninecalculatorapi.dto.food;

import java.math.BigDecimal;

public record FoodResponse(Long id, String name, BigDecimal protein, BigDecimal calories,
                           BigDecimal phenylalanine, String foodTypeName, Integer multiplier) {}
