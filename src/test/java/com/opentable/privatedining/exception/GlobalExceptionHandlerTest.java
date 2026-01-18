package com.opentable.privatedining.exception;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;
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

