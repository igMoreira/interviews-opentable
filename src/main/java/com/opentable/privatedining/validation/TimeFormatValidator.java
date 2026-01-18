package com.opentable.privatedining.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Validator for checking the format of {@link LocalTime} fields.
 * <p>
 * This class uses a specified pattern to validate that a given {@link LocalTime} can be correctly
 * formatted and parsed. The pattern is provided through the {@link TimeFormat} annotation.
 * </p>
 */
public class TimeFormatValidator implements ConstraintValidator<TimeFormat, LocalTime> {

    private String pattern;

    /**
     * Initializes the validator with the pattern from the annotation.
     *
     * @param constraintAnnotation the annotation instance
     */
    @Override
    public void initialize(TimeFormat constraintAnnotation) {
        this.pattern = constraintAnnotation.pattern();
    }

    /**
     * Validates that the LocalTime value can be formatted and parsed using the expected pattern.
     *
     * @param value the value to validate
     * @param context the constraint validator context
     * @return true if valid or null, false otherwise
     */
    @Override
    public boolean isValid(LocalTime value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Let @NotNull handle null validation
        }

        try {
            // Verify the value can be formatted back using the expected pattern
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            String formatted = value.format(formatter);
            LocalTime parsed = LocalTime.parse(formatted, formatter);
            return value.equals(parsed);
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
