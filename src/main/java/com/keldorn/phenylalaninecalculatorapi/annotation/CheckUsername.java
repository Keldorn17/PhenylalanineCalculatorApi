package com.keldorn.phenylalaninecalculatorapi.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.Pattern;

@Documented
@ReportAsSingleViolation
@Constraint(validatedBy = {})
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Pattern(regexp = "^[\\w-]{3,32}$")
public @interface CheckUsername {
    String message() default "Invalid username formatting. Rules: 3-32 characters long without spaces and starts with a letter.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
