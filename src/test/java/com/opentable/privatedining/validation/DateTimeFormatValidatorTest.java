package com.opentable.privatedining.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DateTimeFormatValidatorTest {

    private DateTimeFormatValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new DateTimeFormatValidator();
        context = mock(ConstraintValidatorContext.class);

        // Initialize with the annotation
        DateTimeFormat annotation = mock(DateTimeFormat.class);
        when(annotation.pattern()).thenReturn("dd-MM-yyyy HH:mm");
        validator.initialize(annotation);
    }

    @Test
    void isValid_WhenNullValue_ShouldReturnTrue() {
        // When & Then
        assertTrue(validator.isValid(null, context));
    }

    @Test
    void isValid_WhenValidDateTime_ShouldReturnTrue() {
        // Given
        LocalDateTime validDateTime = LocalDateTime.of(2026, 1, 20, 12, 30);

        // When
        boolean result = validator.isValid(validDateTime, context);

        // Then
        assertTrue(result);
    }

    @Test
    void isValid_WhenDateTimeCanBeFormattedAndParsedBack_ShouldReturnTrue() {
        // Given
        LocalDateTime dateTime = LocalDateTime.of(2026, 6, 15, 14, 45);

        // When
        boolean result = validator.isValid(dateTime, context);

        // Then
        assertTrue(result);
    }

    @Test
    void isValid_WithDifferentPattern_ShouldValidateCorrectly() {
        // Given - use a different pattern
        DateTimeFormat annotation = mock(DateTimeFormat.class);
        when(annotation.pattern()).thenReturn("yyyy-MM-dd'T'HH:mm:ss");
        validator.initialize(annotation);

        LocalDateTime dateTime = LocalDateTime.of(2026, 1, 20, 12, 30, 45);

        // When
        boolean result = validator.isValid(dateTime, context);

        // Then
        assertTrue(result);
    }

    @Test
    void isValid_WhenPatternDoesNotIncludeSeconds_ShouldStillValidate() {
        // Given - pattern without seconds
        DateTimeFormat annotation = mock(DateTimeFormat.class);
        when(annotation.pattern()).thenReturn("dd-MM-yyyy HH:mm");
        validator.initialize(annotation);

        // DateTime with seconds that will be lost in formatting
        LocalDateTime dateTimeWithSeconds = LocalDateTime.of(2026, 1, 20, 12, 30, 45);

        // When - format loses seconds, parsed value won't equal original
        boolean result = validator.isValid(dateTimeWithSeconds, context);

        // Then - should return false because seconds info is lost
        assertFalse(result);
    }

    @Test
    void isValid_WhenDateTimeMatchesPatternExactly_ShouldReturnTrue() {
        // Given - DateTime with no seconds/nanos (matches pattern exactly)
        LocalDateTime dateTime = LocalDateTime.of(2026, 1, 20, 12, 30, 0, 0);

        // When
        boolean result = validator.isValid(dateTime, context);

        // Then
        assertTrue(result);
    }

    @Test
    void isValid_WhenPatternCausesParseException_ShouldReturnFalse() {
        // Given - use a pattern that can format but produces ambiguous output for parsing
        // The pattern with only year will fail to parse back to a full LocalDateTime
        DateTimeFormat annotation = mock(DateTimeFormat.class);
        when(annotation.pattern()).thenReturn("yyyy"); // Only year - can format, but can't parse back to LocalDateTime
        validator.initialize(annotation);

        LocalDateTime dateTime = LocalDateTime.of(2026, 1, 20, 12, 30);

        // When
        boolean result = validator.isValid(dateTime, context);

        // Then - should return false because "2026" can't be parsed back to LocalDateTime
        assertFalse(result);
    }
}

