package com.keldorn.phenylalaninecalculatorapi.mapper;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.FoodType;
import com.keldorn.phenylalaninecalculatorapi.dto.foodtype.FoodTypeRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.foodtype.FoodTypeResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FoodTypeMapper {

    FoodTypeResponse toResponse(FoodType foodType);
    FoodType toEntity(FoodTypeRequest foodTypeRequest);
}
