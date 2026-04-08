package com.keldorn.phenylalaninecalculatorapi.mapper;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.Food;
import com.keldorn.phenylalaninecalculatorapi.dto.food.FoodRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.food.FoodResponse;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FoodMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "phenylalanine", ignore = true)
    @Mapping(target = "foodType", ignore = true)
    @Mapping(target = "user", ignore = true)
    Food toEntity(FoodRequest request);

    @Mapping(source = "foodType.name", target = "foodTypeName")
    @Mapping(source = "foodType.multiplier", target = "multiplier")
    FoodResponse toResponse(Food food);

}
