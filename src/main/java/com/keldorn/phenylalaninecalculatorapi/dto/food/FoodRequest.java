package com.keldorn.phenylalaninecalculatorapi.dto.food;

import com.keldorn.phenylalaninecalculatorapi.annotation.AtLeastOnePresent;

import java.math.BigDecimal;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import lombok.Builder;

@Builder
@AtLeastOnePresent
public record FoodRequest(@NotBlank String name, @NotNull @PositiveOrZero BigDecimal protein,
                          @NotNull @PositiveOrZero BigDecimal calories,
                          @Nullable @PositiveOrZero BigDecimal phenylalanine,
                          @Nullable @PositiveOrZero Long foodTypeId,
                          @Nullable Boolean isPrivate) {

    public FoodRequest(String name, BigDecimal protein, BigDecimal calories, BigDecimal phenylalanine,
            Long foodTypeId) {
        this(name, protein, calories, phenylalanine, foodTypeId, false);
    }

}
