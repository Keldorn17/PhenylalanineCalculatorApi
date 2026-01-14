package com.keldorn.phenylalaninecalculatorapi.dto.food;

import java.math.BigDecimal;

public record FoodUpdateRequest(String name, BigDecimal protein, BigDecimal calories,
                                Long foodTypeId) {}
