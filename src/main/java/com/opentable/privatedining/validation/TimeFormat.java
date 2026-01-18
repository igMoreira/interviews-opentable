package com.opentable.privatedining.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = TimeFormatValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface TimeFormat {

    String message() default "Invalid time format. Expected format: {pattern}";

    String pattern() default "HH:mm";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
