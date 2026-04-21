package com.keldorn.phenylalaninecalculatorapi.mapper;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.FoodType;
import com.keldorn.phenylalaninecalculatorapi.dto.foodtype.FoodTypeRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.foodtype.FoodTypeResponse;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface FoodTypeMapper {

    FoodTypeMapper INSTANCE = Mappers.getMapper(FoodTypeMapper.class);

    FoodTypeResponse toResponse(FoodType foodType);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    FoodType toEntity(FoodTypeRequest foodTypeRequest);

}
