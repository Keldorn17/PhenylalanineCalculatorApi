package com.keldorn.phenylalaninecalculatorapi.mapper;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.Food;
import com.keldorn.phenylalaninecalculatorapi.dto.food.FoodRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.food.FoodResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FoodMapper {

    Food toEntity(FoodRequest request);
    @Mapping(source = "foodType.name", target = "foodTypeName")
    @Mapping(source = "foodType.multiplier", target = "multiplier")
    FoodResponse toResponse(Food food);
}
