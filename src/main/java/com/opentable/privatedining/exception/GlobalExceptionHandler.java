package com.opentable.privatedining.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for handling application-wide exceptions.
 * Provides consistent error responses for validation errors, business logic exceptions,
 * and other runtime exceptions.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles HTTP message not readable exceptions, typically from malformed JSON or invalid date formats.
     *
     * @param ex the exception
     * @param request the web request
     * @return error response with details
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, WebRequest request) {
        logger.warn("Message not readable: {}", ex.getMessage());

        String message = "Invalid request body";
        if (ex.getCause() instanceof InvalidFormatException ife) {
            if (ife.getTargetType().equals(LocalDateTime.class)) {
                message = "Invalid date format. Expected format: dd-MM-yyyy HH:mm";
            } else if (ife.getTargetType().equals(LocalTime.class)) {
                message = "Invalid time format. Expected format: HH:mm";
            }
        }

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", HttpStatus.BAD_REQUEST.value());
        errorDetails.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        errorDetails.put("message", message);
        errorDetails.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles constraint violation exceptions from validation annotations.
     *
     * @param ex the exception
     * @param request the web request
     * @return error response with field errors
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {
        Map<String, String> fieldErrors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> {
                            String path = violation.getPropertyPath().toString();
                            return path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
                        },
                        violation -> violation.getMessage(),
                        (existing, replacement) -> existing
                ));

        logger.warn("Constraint violation: {}", fieldErrors);

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", HttpStatus.BAD_REQUEST.value());
        errorDetails.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        errorDetails.put("message", "Validation failed");
        errorDetails.put("fieldErrors", fieldErrors);
        errorDetails.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles method argument validation exceptions from @Valid annotations.
     *
     * @param ex the exception
     * @param request the web request
     * @return error response with field errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Invalid value",
                        (existing, replacement) -> existing
                ));

        logger.warn("Validation failed: {}", fieldErrors);

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", HttpStatus.BAD_REQUEST.value());
        errorDetails.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        errorDetails.put("message", "Validation failed");
        errorDetails.put("fieldErrors", fieldErrors);
        errorDetails.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles restaurant not found exceptions.
     *
     * @param ex the exception
     * @param request the web request
     * @return error response with 404 status
     */
    @ExceptionHandler(RestaurantNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleRestaurantNotFound(
            RestaurantNotFoundException ex, WebRequest request) {
        logger.warn("Restaurant not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    /**
     * Handles space not found exceptions.
     *
     * @param ex the exception
     * @param request the web request
     * @return error response with 404 status
     */
    @ExceptionHandler(SpaceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleSpaceNotFound(
            SpaceNotFoundException ex, WebRequest request) {
        logger.warn("Space not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    /**
     * Handles reservation not found exceptions.
     *
     * @param ex the exception
     * @param request the web request
     * @return error response with 404 status
     */
    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleReservationNotFound(
            ReservationNotFoundException ex, WebRequest request) {
        logger.warn("Reservation not found: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    /**
     * Handles invalid party size exceptions.
     *
     * @param ex the exception
     * @param request the web request
     * @return error response with 400 status
     */
    @ExceptionHandler(InvalidPartySizeException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidPartySize(
            InvalidPartySizeException ex, WebRequest request) {
        logger.warn("Invalid party size: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /**
     * Handles reservation conflict exceptions.
     *
     * @param ex the exception
     * @param request the web request
     * @return error response with 409 status
     */
    @ExceptionHandler(ReservationConflictException.class)
    public ResponseEntity<Map<String, Object>> handleReservationConflict(
            ReservationConflictException ex, WebRequest request) {
        logger.warn("Reservation conflict: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    /**
     * Handles capacity exceeded exceptions.
     *
     * @param ex the exception
     * @param request the web request
     * @return error response with 409 status
     */
    @ExceptionHandler(CapacityExceededException.class)
    public ResponseEntity<Map<String, Object>> handleCapacityExceeded(
            CapacityExceededException ex, WebRequest request) {
        logger.warn("Capacity exceeded: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    /**
     * Handles outside operating hours exceptions.
     *
     * @param ex the exception
     * @param request the web request
     * @return error response with 400 status
     */
    @ExceptionHandler(OutsideOperatingHoursException.class)
    public ResponseEntity<Map<String, Object>> handleOutsideOperatingHours(
            OutsideOperatingHoursException ex, WebRequest request) {
        logger.warn("Outside operating hours: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /**
     * Handles multi-day reservation exceptions.
     *
     * @param ex the exception
     * @param request the web request
     * @return error response with 400 status
     */
    @ExceptionHandler(MultiDayReservationException.class)
    public ResponseEntity<Map<String, Object>> handleMultiDayReservation(
            MultiDayReservationException ex, WebRequest request) {
        logger.warn("Multi-day reservation: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /**
     * Handles invalid reservation duration exceptions.
     *
     * @param ex the exception
     * @param request the web request
     * @return error response with 400 status
     */
    @ExceptionHandler(InvalidReservationDurationException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidReservationDuration(
            InvalidReservationDurationException ex, WebRequest request) {
        logger.warn("Invalid reservation duration: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /**
     * Handles invalid date range exceptions.
     *
     * @param ex the exception
     * @param request the web request
     * @return error response with 400 status
     */
    @ExceptionHandler(InvalidDateRangeException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidDateRange(
            InvalidDateRangeException ex, WebRequest request) {
        logger.warn("Invalid date range: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /**
     * Handles illegal argument exceptions.
     *
     * @param ex the exception
     * @param request the web request
     * @return error response with 400 status
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex, WebRequest request) {
        logger.warn("Invalid argument: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Invalid request parameters", request);
    }

    /**
     * Builds a standard error response.
     *
     * @param status the HTTP status
     * @param message the error message
     * @param request the web request
     * @return error response entity
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status, String message, WebRequest request) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", status.value());
        errorDetails.put("error", status.getReasonPhrase());
        errorDetails.put("message", message);
        errorDetails.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(errorDetails, status);
    }
}