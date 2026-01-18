package com.opentable.privatedining.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Validator for checking if a {@link LocalDateTime} field is in the correct format
 * as specified by the {@link DateTimeFormat} annotation.
 */
public class DateTimeFormatValidator implements ConstraintValidator<DateTimeFormat, LocalDateTime> {

    private String pattern;

    /**
     * Initializes the validator with the pattern from the annotation.
     *
     * @param constraintAnnotation the annotation instance
     */
    @Override
    public void initialize(DateTimeFormat constraintAnnotation) {
        this.pattern = constraintAnnotation.pattern();
    }

    /**
     * Validates that the LocalDateTime value can be formatted and parsed using the expected pattern.
     *
     * @param value the value to validate
     * @param context the constraint validator context
     * @return true if valid or null, false otherwise
     */
    @Override
    public boolean isValid(LocalDateTime value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Let @NotNull handle null validation
        }

        try {
            // Verify the value can be formatted back using the expected pattern
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            String formatted = value.format(formatter);
            LocalDateTime parsed = LocalDateTime.parse(formatted, formatter);
            return value.equals(parsed);
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
