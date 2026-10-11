package com.keldorn.phenylalaninecalculatorapi.annotation;

import com.keldorn.phenylalaninecalculatorapi.validator.AtLeastOnePresentValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AtLeastOnePresentValidator.class)
public @interface AtLeastOnePresent {

    String message() default "Either phenylalanine or foodTypeId must be provided";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
