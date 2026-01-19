package com.keldorn.phenylalaninecalculatorapi.mapper;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.FoodConsumption;
import com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption.FoodConsumptionRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption.FoodConsumptionResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FoodConsumptionMapper {

    FoodConsumptionResponse toResponse(FoodConsumption foodConsumption);
    FoodConsumption toEntity(FoodConsumptionRequest request);
}
