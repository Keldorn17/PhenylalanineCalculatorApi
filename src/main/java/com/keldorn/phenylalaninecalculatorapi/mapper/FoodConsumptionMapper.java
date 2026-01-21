package com.keldorn.phenylalaninecalculatorapi.mapper;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.FoodConsumption;
import com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption.FoodConsumptionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Mapper(componentModel = "spring")
public interface FoodConsumptionMapper {

    @Mapping(target = "consumedAt", expression = "java(mapToLocalDateTime(foodConsumption))")
    FoodConsumptionResponse toResponse(FoodConsumption foodConsumption);

    default LocalDateTime mapToLocalDateTime(FoodConsumption foodConsumption) {
        if (foodConsumption.getConsumedAt() == null) {
            return null;
        }

        ZoneId userZone = foodConsumption.getUser().resolveZoneId();

        return foodConsumption.getConsumedAt()
                .atZone(userZone)
                .toLocalDateTime();
    }
}
