package com.keldorn.phenylalaninecalculatorapi.mapper;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.Food;
import com.keldorn.phenylalaninecalculatorapi.dto.food.FoodRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.food.FoodResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.food.FoodUpdateRequest;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface FoodMapper {

    FoodMapper INSTANCE = Mappers.getMapper(FoodMapper.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "phenylalanine", ignore = true)
    @Mapping(target = "foodType", ignore = true)
    @Mapping(target = "user", ignore = true)
    Food toEntity(FoodRequest request);

    @Mapping(source = "foodType.name", target = "foodTypeName")
    @Mapping(source = "foodType.multiplier", target = "multiplier")
    FoodResponse toResponse(Food food);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(FoodUpdateRequest request, @MappingTarget Food food);

}
