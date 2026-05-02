package com.keldorn.phenylalaninecalculatorapi.utils;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.Food;
import com.keldorn.phenylalaninecalculatorapi.dto.params.QueryRequest;

import java.util.HashMap;
import java.util.Map;

import lombok.experimental.UtilityClass;

import org.springframework.data.jpa.domain.Specification;

import io.github.perplexhub.rsql.RSQLJPASupport;

@UtilityClass
public class FoodQueryParamsUtil {

    private static final Map<String, String> PROPERTY_MAP = new HashMap<>();

    static {
        PROPERTY_MAP.put("id", "id");
        PROPERTY_MAP.put("name", "name");
        PROPERTY_MAP.put("protein", "protein");
        PROPERTY_MAP.put("calories", "calories");
        PROPERTY_MAP.put("phenylalanine", "phenylalanine");
        PROPERTY_MAP.put("foodTypeName", "foodType.name");
        PROPERTY_MAP.put("foodTypeMultiplier", "foodType.multiplier");
        PROPERTY_MAP.put("username", "user.username");
    }

    public static Specification<Food> createQuerySpecification(QueryRequest request) {
        Specification<Food> spec = ((_, _, criteriaBuilder) -> criteriaBuilder.conjunction());
        if (isNotBlank(request.getQuery())) {
            spec = spec.and(RSQLJPASupport.toSpecification(request.getQuery(), PROPERTY_MAP));
        }
        if (isNotBlank(request.getSort())) {
            spec = spec.and(RSQLJPASupport.toSort(request.getSort(), PROPERTY_MAP));
        }
        return spec;
    }

    private static boolean isNotBlank(String input) {
        return input != null && !input.isBlank();
    }

}
