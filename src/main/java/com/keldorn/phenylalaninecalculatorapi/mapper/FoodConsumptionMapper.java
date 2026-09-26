package com.keldorn.phenylalaninecalculatorapi.mapper;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.FoodConsumption;
import com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption.FoodConsumptionResponse;
import com.keldorn.phenylalaninecalculatorapi.dto.foodconsumption.PagedFoodConsumptionResponse;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface FoodConsumptionMapper {

    FoodConsumptionMapper INSTANCE = Mappers.getMapper(FoodConsumptionMapper.class);

    @Mapping(source = ".", target = "page")
    @Mapping(source = "content", target = "content")
    PagedFoodConsumptionResponse toModel(Page<FoodConsumption> savedPost);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "amount", target = "amount")
    @Mapping(source = "food.name", target = "foodName")
    @Mapping(source = "consumedAt", target = "consumedAt")
    @Mapping(source = "phenylalanineAmount", target = "phenylalanineAmount")
    FoodConsumptionResponse toModel(FoodConsumption foodConsumption);

}
