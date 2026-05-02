package com.keldorn.phenylalaninecalculatorapi.mapper;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.Food;
import com.keldorn.phenylalaninecalculatorapi.dto.food.FoodRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.food.FoodResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.food.FoodUpdateRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.food.PagedFoodResponse;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface FoodMapper {

    FoodMapper INSTANCE = Mappers.getMapper(FoodMapper.class);

    @Mapping(source = ".", target = "page")
    @Mapping(source = "content", target = "content")
    PagedFoodResponse toModel(Page<Food> savedPost);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "foodType", ignore = true)
    @Mapping(target = "phenylalanine", ignore = true)
    @Mapping(target = "foodConsumption", ignore = true)
    @Mapping(source = "name", target = "name")
    @Mapping(source = "protein", target = "protein")
    @Mapping(source = "calories", target = "calories")
    Food toEntity(FoodRequest request);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "protein", target = "protein")
    @Mapping(source = "calories", target = "calories")
    @Mapping(source = "foodType.name", target = "foodTypeName")
    @Mapping(source = "phenylalanine", target = "phenylalanine")
    @Mapping(source = "foodType.multiplier", target = "multiplier")
    FoodResponse toModel(Food food);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "foodType", ignore = true)
    @Mapping(target = "phenylalanine", ignore = true)
    @Mapping(target = "foodConsumption", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(FoodUpdateRequest request, @MappingTarget Food food);

}
