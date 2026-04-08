package com.keldorn.phenylalaninecalculatorapi.mapper;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.FoodConsumption;
import com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption.FoodConsumptionResponse;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.mapstruct.Context;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FoodConsumptionMapper {

    FoodConsumptionResponse toResponse(FoodConsumption foodConsumption, @Context ZoneId timezone);

    default LocalDateTime mapInstantToLocalDateTime(Instant consumedAt, @Context ZoneId timezone) {
        if (consumedAt == null) {
            return null;
        }
        return consumedAt.atZone(timezone).toLocalDateTime();
    }

}
