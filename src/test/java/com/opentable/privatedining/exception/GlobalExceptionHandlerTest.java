package com.opentable.privatedining.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        webRequest = mock(WebRequest.class);
        when(webRequest.getDescription(false)).thenReturn("uri=/api/test");
    }

    // ==================== HttpMessageNotReadableException Tests ====================

    @Test
    void handleHttpMessageNotReadable_WithGenericCause_ShouldReturnInvalidRequestBody() {
        // Given
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
            "Could not read document", new RuntimeException("Generic error"), null);

        // When
        ResponseEntity<Map<String, Object>> response = handler.handleHttpMessageNotReadable(ex, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().get("status"));
        assertEquals("Invalid request body", response.getBody().get("message"));
        assertEquals("/api/test", response.getBody().get("path"));
    }

    @Test
    void handleHttpMessageNotReadable_WithLocalDateTimeFormatException_ShouldReturnDateFormatError() {
        // Given
        InvalidFormatException ife = mock(InvalidFormatException.class);
        when(ife.getTargetType()).thenReturn((Class) LocalDateTime.class);
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
            "Invalid date format", ife, null);

        // When
        ResponseEntity<Map<String, Object>> response = handler.handleHttpMessageNotReadable(ex, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid date format. Expected format: dd-MM-yyyy HH:mm", response.getBody().get("message"));
    }

    @Test
    void handleHttpMessageNotReadable_WithLocalTimeFormatException_ShouldReturnTimeFormatError() {
        // Given
        InvalidFormatException ife = mock(InvalidFormatException.class);
        when(ife.getTargetType()).thenReturn((Class) LocalTime.class);
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
            "Invalid time format", ife, null);

        // When
        ResponseEntity<Map<String, Object>> response = handler.handleHttpMessageNotReadable(ex, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid time format. Expected format: HH:mm", response.getBody().get("message"));
    }

    @Test
    void handleHttpMessageNotReadable_WithOtherFormatException_ShouldReturnInvalidRequestBody() {
        // Given - InvalidFormatException for a different type (e.g., Integer)
        InvalidFormatException ife = mock(InvalidFormatException.class);
        when(ife.getTargetType()).thenReturn((Class) Integer.class);
        HttpMessageNotReadableException ex = new HttpMessageNotReadableException(
            "Invalid integer format", ife, null);

        // When
        ResponseEntity<Map<String, Object>> response = handler.handleHttpMessageNotReadable(ex, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid request body", response.getBody().get("message"));
    }

    @Test
    void handleHttpMessageNotReadable_WithNullCause_ShouldReturnInvalidRequestBody() {
        // Given
        HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
        when(ex.getCause()).thenReturn(null);
        when(ex.getMessage()).thenReturn("Some error");

        // When
        ResponseEntity<Map<String, Object>> response = handler.handleHttpMessageNotReadable(ex, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid request body", response.getBody().get("message"));
    }

    // ==================== MethodArgumentNotValidException Tests ====================

    @Test
    void handleValidationExceptions_WithFieldErrors_ShouldReturnFieldErrorsMap() {
        // Given
        FieldError fieldError1 = new FieldError("object", "fieldName", "must not be null");
        FieldError fieldError2 = new FieldError("object", "email", "must be a valid email");

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        // When
        ResponseEntity<Map<String, Object>> response = handler.handleValidationExceptions(ex, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().get("status"));
        assertEquals("Validation failed", response.getBody().get("message"));

        @SuppressWarnings("unchecked")
        Map<String, String> fieldErrors = (Map<String, String>) response.getBody().get("fieldErrors");
        assertNotNull(fieldErrors);
        assertEquals("must not be null", fieldErrors.get("fieldName"));
        assertEquals("must be a valid email", fieldErrors.get("email"));
    }

    @Test
    void handleValidationExceptions_WithNullDefaultMessage_ShouldReturnInvalidValue() {
        // Given
        FieldError fieldError = new FieldError("object", "fieldName", null);

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        // When
        ResponseEntity<Map<String, Object>> response = handler.handleValidationExceptions(ex, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        @SuppressWarnings("unchecked")
        Map<String, String> fieldErrors = (Map<String, String>) response.getBody().get("fieldErrors");
        assertNotNull(fieldErrors);
        assertEquals("Invalid value", fieldErrors.get("fieldName"));
    }

    @Test
    void handleValidationExceptions_WithDuplicateFieldNames_ShouldKeepFirst() {
        // Given - duplicate field names with different messages
        FieldError fieldError1 = new FieldError("object", "fieldName", "first error");
        FieldError fieldError2 = new FieldError("object", "fieldName", "second error");

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        // When
        ResponseEntity<Map<String, Object>> response = handler.handleValidationExceptions(ex, webRequest);

        // Then
        @SuppressWarnings("unchecked")
        Map<String, String> fieldErrors = (Map<String, String>) response.getBody().get("fieldErrors");
        assertEquals(1, fieldErrors.size());
        assertEquals("first error", fieldErrors.get("fieldName"));
    }

    // ==================== ConstraintViolationException Tests ====================

    @Test
    void handleConstraintViolation_WithSimplePath_ShouldReturnFieldName() {
        // Given
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("fieldName");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must not be blank");

        Set<ConstraintViolation<?>> violations = new HashSet<>();
        violations.add(violation);
        ConstraintViolationException ex = new ConstraintViolationException(violations);

        // When
        ResponseEntity<Map<String, Object>> response = handler.handleConstraintViolation(ex, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation failed", response.getBody().get("message"));

        @SuppressWarnings("unchecked")
        Map<String, String> fieldErrors = (Map<String, String>) response.getBody().get("fieldErrors");
        assertNotNull(fieldErrors);
        assertEquals("must not be blank", fieldErrors.get("fieldName"));
    }

    @Test
    void handleConstraintViolation_WithNestedPath_ShouldExtractLastSegment() {
        // Given
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("createReservation.request.partySize");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must be greater than 0");

        Set<ConstraintViolation<?>> violations = new HashSet<>();
        violations.add(violation);
        ConstraintViolationException ex = new ConstraintViolationException(violations);

        // When
        ResponseEntity<Map<String, Object>> response = handler.handleConstraintViolation(ex, webRequest);

        // Then
        @SuppressWarnings("unchecked")
        Map<String, String> fieldErrors = (Map<String, String>) response.getBody().get("fieldErrors");
        assertNotNull(fieldErrors);
        assertEquals("must be greater than 0", fieldErrors.get("partySize"));
    }

    @Test
    void handleConstraintViolation_WithDuplicateViolations_ShouldKeepFirst() {
        // Given - multiple violations on same field
        ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
        Path path1 = mock(Path.class);
        when(path1.toString()).thenReturn("email");
        when(violation1.getPropertyPath()).thenReturn(path1);
        when(violation1.getMessage()).thenReturn("first message");

        ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);
        Path path2 = mock(Path.class);
        when(path2.toString()).thenReturn("email");
        when(violation2.getPropertyPath()).thenReturn(path2);
        when(violation2.getMessage()).thenReturn("second message");

        Set<ConstraintViolation<?>> violations = new HashSet<>();
        violations.add(violation1);
        violations.add(violation2);
        ConstraintViolationException ex = new ConstraintViolationException(violations);

        // When
        ResponseEntity<Map<String, Object>> response = handler.handleConstraintViolation(ex, webRequest);

        // Then
        @SuppressWarnings("unchecked")
        Map<String, String> fieldErrors = (Map<String, String>) response.getBody().get("fieldErrors");
        assertEquals(1, fieldErrors.size());
        assertTrue(fieldErrors.containsKey("email"));
    }

    // ==================== RestaurantNotFoundException Tests ====================

    @Test
    void handleRestaurantNotFound_ShouldReturnNotFound() {
        // Given
        RestaurantNotFoundException ex = new RestaurantNotFoundException(new ObjectId());

        // When
        ResponseEntity<Map<String, Object>> response = handler.handleRestaurantNotFound(ex, webRequest);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().get("status"));
        assertEquals("Not Found", response.getBody().get("error"));
        assertEquals("/api/test", response.getBody().get("path"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    // ==================== SpaceNotFoundException Tests ====================

    @Test
    void handleSpaceNotFound_ShouldReturnNotFound() {
        // Given
        SpaceNotFoundException ex = new SpaceNotFoundException(new ObjectId(), UUID.randomUUID());

        // When
        ResponseEntity<Map<String, Object>> response = handler.handleSpaceNotFound(ex, webRequest);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().get("status"));
        assertEquals("Not Found", response.getBody().get("error"));
    }

    // ==================== ReservationNotFoundException Tests ====================

    @Test
    void handleReservationNotFound_ShouldReturnNotFound() {
        // Given
        ReservationNotFoundException ex = new ReservationNotFoundException(new ObjectId());

        // When
        ResponseEntity<Map<String, Object>> response = handler.handleReservationNotFound(ex, webRequest);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().get("status"));
        assertEquals("Not Found", response.getBody().get("error"));
    }

    // ==================== InvalidPartySizeException Tests ====================

    @Test
    void handleInvalidPartySize_ShouldReturnBadRequest() {
        // Given
        InvalidPartySizeException ex = new InvalidPartySizeException(1, 2, 10);

        // When
        ResponseEntity<Map<String, Object>> response = handler.handleInvalidPartySize(ex, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().get("status"));
        assertEquals("Bad Request", response.getBody().get("error"));
        assertTrue(response.getBody().get("message").toString().contains("1"));
    }

    // ==================== ReservationConflictException Tests ====================

    @Test
    void handleReservationConflict_ShouldReturnConflict() {
        // Given
        ReservationConflictException ex = new ReservationConflictException(
            new ObjectId(), UUID.randomUUID(),
            LocalDateTime.of(2026, 1, 20, 12, 0),
            LocalDateTime.of(2026, 1, 20, 14, 0));

        // When
        ResponseEntity<Map<String, Object>> response = handler.handleReservationConflict(ex, webRequest);

        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().get("status"));
        assertEquals("Conflict", response.getBody().get("error"));
    }

    // ==================== CapacityExceededException Tests ====================

    @Test
    void handleCapacityExceeded_ShouldReturnConflict() {
        // Given
        CapacityExceededException ex = new CapacityExceededException(
            new ObjectId(), UUID.randomUUID(),
            LocalDateTime.of(2026, 1, 20, 12, 0),
            LocalDateTime.of(2026, 1, 20, 14, 0),
            6, 8, 10);

        // When
        ResponseEntity<Map<String, Object>> response = handler.handleCapacityExceeded(ex, webRequest);

        // Then
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().get("status"));
        assertEquals("Conflict", response.getBody().get("error"));
        assertTrue(response.getBody().get("message").toString().contains("Cannot accommodate"));
    }

    // ==================== OutsideOperatingHoursException Tests ====================

    @Test
    void handleOutsideOperatingHours_ShouldReturnBadRequest() {
        // Given
        OutsideOperatingHoursException ex = new OutsideOperatingHoursException(
            LocalTime.of(8, 0), LocalTime.of(10, 0),
            LocalTime.of(9, 0), LocalTime.of(22, 0));

        // When
        ResponseEntity<Map<String, Object>> response = handler.handleOutsideOperatingHours(ex, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().get("status"));
        assertEquals("Bad Request", response.getBody().get("error"));
    }

    // ==================== MultiDayReservationException Tests ====================

    @Test
    void handleMultiDayReservation_ShouldReturnBadRequest() {
        // Given
        MultiDayReservationException ex = new MultiDayReservationException(
            LocalDate.of(2026, 1, 20),
            LocalDate.of(2026, 1, 21));

        // When
        ResponseEntity<Map<String, Object>> response = handler.handleMultiDayReservation(ex, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().get("status"));
        assertEquals("Bad Request", response.getBody().get("error"));
    }

    // ==================== InvalidReservationDurationException Tests ====================

    @Test
    void handleInvalidReservationDuration_ShouldReturnBadRequest() {
        // Given
        InvalidReservationDurationException ex = new InvalidReservationDurationException(60);

        // When
        ResponseEntity<Map<String, Object>> response = handler.handleInvalidReservationDuration(ex, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().get("status"));
        assertEquals("Bad Request", response.getBody().get("error"));
        assertTrue(response.getBody().get("message").toString().contains("60"));
    }

    // ==================== InvalidDateRangeException Tests ====================

    @Test
    void handleInvalidDateRange_ShouldReturnBadRequest() {
        // Given
        InvalidDateRangeException ex = new InvalidDateRangeException(
            LocalDateTime.of(2026, 1, 20, 12, 0),
            LocalDateTime.of(2026, 1, 10, 12, 0),
            "End time must be after start time");

        // When
        ResponseEntity<Map<String, Object>> response = handler.handleInvalidDateRange(ex, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().get("status"));
        assertEquals("Bad Request", response.getBody().get("error"));
    }

    // ==================== IllegalArgumentException Tests ====================

    @Test
    void handleIllegalArgument_ShouldReturnBadRequest() {
        // Given
        IllegalArgumentException ex = new IllegalArgumentException("Invalid parameter value");

        // When
        ResponseEntity<Map<String, Object>> response = handler.handleIllegalArgument(ex, webRequest);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().get("status"));
        assertEquals("Bad Request", response.getBody().get("error"));
        assertEquals("Invalid request parameters", response.getBody().get("message"));
    }

    // ==================== Error Response Structure Tests ====================

    @Test
    void handleException_ShouldBuildCorrectErrorResponseStructure() {
        // Given
        RestaurantNotFoundException ex = new RestaurantNotFoundException(new ObjectId());
        when(webRequest.getDescription(false)).thenReturn("uri=/api/restaurants/123");

        // When
        ResponseEntity<Map<String, Object>> response = handler.handleRestaurantNotFound(ex, webRequest);

        // Then
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("timestamp"));
        assertTrue(body.containsKey("status"));
        assertTrue(body.containsKey("error"));
        assertTrue(body.containsKey("message"));
        assertTrue(body.containsKey("path"));
        assertEquals("/api/restaurants/123", body.get("path"));
    }
}

