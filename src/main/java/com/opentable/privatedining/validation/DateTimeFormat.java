package com.opentable.privatedining.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for validating the format of date-time fields.
 * <p>
 * This annotation can be applied to fields or parameters of type {@link java.time.LocalDateTime}
 * to ensure they conform to a specified date-time pattern. The default pattern is "dd-MM-yyyy HH:mm".
 * </p>
 */
@Documented
@Constraint(validatedBy = DateTimeFormatValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface DateTimeFormat {

    /**
     * The error message to return when validation fails.
     *
     * @return the error message
     */
    String message() default "Invalid date format. Expected format: {pattern}";

    /**
     * The date-time pattern to validate against.
     *
     * @return the pattern string
     */
    String pattern() default "dd-MM-yyyy HH:mm";

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
