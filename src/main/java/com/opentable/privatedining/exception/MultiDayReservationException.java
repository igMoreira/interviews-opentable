package com.opentable.privatedining.exception;

import java.time.LocalDate;

/**
 * Exception thrown when a reservation spans multiple days, which is not allowed.
 */
public class MultiDayReservationException extends RuntimeException {

    /**
     * Constructs a new MultiDayReservationException with the conflicting dates.
     *
     * @param startDate the start date of the reservation
     * @param endDate the end date of the reservation
     */
    public MultiDayReservationException(LocalDate startDate, LocalDate endDate) {
        super(String.format(
            "Multi-day reservations are not allowed. Start date: %s, End date: %s",
            startDate, endDate));
    }
}
