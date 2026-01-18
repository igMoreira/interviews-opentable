package com.opentable.privatedining.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for validating the format of time fields.
 * <p>
 * This annotation can be applied to fields or parameters of type {@link java.time.LocalTime}
 * to ensure they conform to a specified time pattern. The default pattern is "HH:mm".
 * </p>
 */
@Documented
@Constraint(validatedBy = TimeFormatValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface TimeFormat {

    /**
     * The error message to return when validation fails.
     *
     * @return the error message
     */
    String message() default "Invalid time format. Expected format: {pattern}";

    /**
     * The time pattern to validate against.
     *
     * @return the pattern string
     */
    String pattern() default "HH:mm";

    /**
     * The validation groups this constraint belongs to.
     *
     * @return the groups
     */
    Class<?>[] groups() default {};

    /**
     * The payload associated with this constraint.
     *
     * @return the payload
     */
    Class<? extends Payload>[] payload() default {};
}
