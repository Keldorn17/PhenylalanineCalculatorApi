package com.keldorn.phenylalaninecalculatorapi.mapper;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.FoodType;
import com.keldorn.phenylalaninecalculatorapi.dto.foodtype.FoodTypeRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.foodtype.FoodTypeResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.foodtype.PagedFoodTypeResponse;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface FoodTypeMapper {

    FoodTypeMapper INSTANCE = Mappers.getMapper(FoodTypeMapper.class);

    @Mapping(source = ".", target = "page")
    @Mapping(source = "content", target = "content")
    PagedFoodTypeResponse toModel(Page<FoodType> savedPost);

    FoodTypeResponse toModel(FoodType foodType);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    FoodType toEntity(FoodTypeRequest foodTypeRequest);

}
