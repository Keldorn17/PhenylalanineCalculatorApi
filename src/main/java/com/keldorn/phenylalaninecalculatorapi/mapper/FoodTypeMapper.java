package com.keldorn.phenylalaninecalculatorapi.mapper;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.FoodType;
import com.keldorn.phenylalaninecalculatorapi.dto.foodtype.FoodTypeRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.foodtype.FoodTypeResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.foodtype.PagedFoodTypeResponse;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface FoodTypeMapper {

    FoodTypeMapper INSTANCE = Mappers.getMapper(FoodTypeMapper.class);

    @Mapping(source = ".", target = "page")
    @Mapping(source = "content", target = "content")
    PagedFoodTypeResponse toModel(Page<FoodType> savedPost, @Context Long currentUserId);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "multiplier", target = "multiplier")
    @Mapping(target = "canEdit",
            expression = "java(foodType.getUser() != null && foodType.getUser().getUserId().equals(currentUserId))")
    FoodTypeResponse toModel(FoodType foodType, @Context Long currentUserId);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    FoodType toEntity(FoodTypeRequest foodTypeRequest);

}
