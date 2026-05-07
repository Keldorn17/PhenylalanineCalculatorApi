package com.keldorn.phenylalaninecalculatorapi.dto.foodtype;

import lombok.Builder;

@Builder
public record FoodTypeResponse(Long id, String name, Integer multiplier, boolean canEdit) {}
