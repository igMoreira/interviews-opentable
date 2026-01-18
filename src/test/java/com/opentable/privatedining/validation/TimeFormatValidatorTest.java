package com.opentable.privatedining.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TimeFormatValidatorTest {

    private TimeFormatValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new TimeFormatValidator();
        context = mock(ConstraintValidatorContext.class);

        // Initialize with the annotation
        TimeFormat annotation = mock(TimeFormat.class);
        when(annotation.pattern()).thenReturn("HH:mm");
        validator.initialize(annotation);
    }

    @Test
    void isValid_WhenNullValue_ShouldReturnTrue() {
        // When & Then
        assertTrue(validator.isValid(null, context));
    }

    @Test
    void isValid_WhenValidTime_ShouldReturnTrue() {
        // Given
        LocalTime validTime = LocalTime.of(12, 30);

        // When
        boolean result = validator.isValid(validTime, context);

        // Then
        assertTrue(result);
    }

    @Test
    void isValid_WhenTimeCanBeFormattedAndParsedBack_ShouldReturnTrue() {
        // Given
        LocalTime time = LocalTime.of(14, 45);

        // When
        boolean result = validator.isValid(time, context);

        // Then
        assertTrue(result);
    }

    @Test
    void isValid_WithDifferentPattern_ShouldValidateCorrectly() {
        // Given - use a different pattern
        TimeFormat annotation = mock(TimeFormat.class);
        when(annotation.pattern()).thenReturn("HH:mm:ss");
        validator.initialize(annotation);

        LocalTime time = LocalTime.of(12, 30, 45);

        // When
        boolean result = validator.isValid(time, context);

        // Then
        assertTrue(result);
    }

    @Test
    void isValid_WhenPatternDoesNotIncludeSeconds_ShouldStillValidate() {
        // Given - pattern without seconds
        TimeFormat annotation = mock(TimeFormat.class);
        when(annotation.pattern()).thenReturn("HH:mm");
        validator.initialize(annotation);

        // Time with seconds that will be lost in formatting
        LocalTime timeWithSeconds = LocalTime.of(12, 30, 45);

        // When - format loses seconds, parsed value won't equal original
        boolean result = validator.isValid(timeWithSeconds, context);

        // Then - should return false because seconds info is lost
        assertFalse(result);
    }

    @Test
    void isValid_WhenTimeMatchesPatternExactly_ShouldReturnTrue() {
        // Given - Time with no seconds/nanos (matches pattern exactly)
        LocalTime time = LocalTime.of(12, 30, 0, 0);

        // When
        boolean result = validator.isValid(time, context);

        // Then
        assertTrue(result);
    }

    @Test
    void isValid_WhenPatternCausesParseException_ShouldReturnFalse() {
        // Given - use a pattern that can format but produces ambiguous output for parsing
        // The pattern with only hour will fail to parse back to a full LocalTime
        TimeFormat annotation = mock(TimeFormat.class);
        when(annotation.pattern()).thenReturn("HH"); // Only hour - can format, but can't parse back to LocalTime
        validator.initialize(annotation);

        LocalTime time = LocalTime.of(12, 30);

        // When
        boolean result = validator.isValid(time, context);

        // Then - should return false because "12" can't be parsed back to LocalTime
        assertFalse(result);
    }
}

