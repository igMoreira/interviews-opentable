package com.opentable.privatedining.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateTimeFormatValidator implements ConstraintValidator<DateTimeFormat, LocalDateTime> {

    private String pattern;

    @Override
    public void initialize(DateTimeFormat constraintAnnotation) {
        this.pattern = constraintAnnotation.pattern();
    }

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

