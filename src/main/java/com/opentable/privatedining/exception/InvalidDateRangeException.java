package com.opentable.privatedining.exception;

import java.time.LocalDateTime;

/**
 * Exception thrown when an invalid date range is provided for analytics queries.
 */
public class InvalidDateRangeException extends RuntimeException {

    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final Integer maxRangeDays;

    /**
     * Constructs a new InvalidDateRangeException with a custom message.
     *
     * @param message the detail message
     */
    public InvalidDateRangeException(String message) {
        super(message);
        this.startTime = null;
        this.endTime = null;
        this.maxRangeDays = null;
    }

    /**
     * Constructs a new InvalidDateRangeException with date range and reason.
     *
     * @param startTime the start time of the invalid range
     * @param endTime the end time of the invalid range
     * @param reason the reason why the range is invalid
     */
    public InvalidDateRangeException(LocalDateTime startTime, LocalDateTime endTime, String reason) {
        super(String.format("Invalid date range from %s to %s: %s", startTime, endTime, reason));
        this.startTime = startTime;
        this.endTime = endTime;
        this.maxRangeDays = null;
    }

    /**
     * Constructs a new InvalidDateRangeException when the range exceeds maximum days.
     *
     * @param startTime the start time of the range
     * @param endTime the end time of the range
     * @param maxRangeDays the maximum allowed range in days
     */
    public InvalidDateRangeException(LocalDateTime startTime, LocalDateTime endTime, Integer maxRangeDays) {
        super(String.format("Date range from %s to %s exceeds maximum allowed range of %d days",
                startTime, endTime, maxRangeDays));
        this.startTime = startTime;
        this.endTime = endTime;
        this.maxRangeDays = maxRangeDays;
    }

    /**
     * Gets the start time of the invalid range.
     *
     * @return the start time, or null if not specified
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }

    /**
     * Gets the end time of the invalid range.
     *
     * @return the end time, or null if not specified
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }

    /**
     * Gets the maximum range days that was exceeded.
     *
     * @return the maximum range days, or null if not applicable
     */
    public Integer getMaxRangeDays() {
        return maxRangeDays;
    }
}
