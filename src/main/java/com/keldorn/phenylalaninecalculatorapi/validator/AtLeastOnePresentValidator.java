package com.keldorn.phenylalaninecalculatorapi.validator;

import com.keldorn.phenylalaninecalculatorapi.annotation.AtLeastOnePresent;
import com.keldorn.phenylalaninecalculatorapi.dto.food.FoodRequest;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AtLeastOnePresentValidator implements ConstraintValidator<AtLeastOnePresent, FoodRequest> {

    @Override
    public boolean isValid(FoodRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }
        return request.phenylalanine() != null || request.foodTypeId() != null;
    }

}
