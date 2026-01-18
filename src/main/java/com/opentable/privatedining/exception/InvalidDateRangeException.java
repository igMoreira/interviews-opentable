package com.opentable.privatedining.exception;

import java.time.LocalDateTime;

/**
 * Exception thrown when an invalid date range is provided for analytics queries.
 */
public class InvalidDateRangeException extends RuntimeException {

    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final Integer maxRangeDays;

    public InvalidDateRangeException(String message) {
        super(message);
        this.startTime = null;
        this.endTime = null;
        this.maxRangeDays = null;
    }

    public InvalidDateRangeException(LocalDateTime startTime, LocalDateTime endTime, String reason) {
        super(String.format("Invalid date range from %s to %s: %s", startTime, endTime, reason));
        this.startTime = startTime;
        this.endTime = endTime;
        this.maxRangeDays = null;
    }

    public InvalidDateRangeException(LocalDateTime startTime, LocalDateTime endTime, Integer maxRangeDays) {
        super(String.format("Date range from %s to %s exceeds maximum allowed range of %d days",
                startTime, endTime, maxRangeDays));
        this.startTime = startTime;
        this.endTime = endTime;
        this.maxRangeDays = maxRangeDays;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public Integer getMaxRangeDays() {
        return maxRangeDays;
    }
}

