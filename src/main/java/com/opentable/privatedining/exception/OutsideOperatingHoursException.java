package com.opentable.privatedining.exception;

import java.time.LocalTime;

/**
 * Exception thrown when a reservation time falls outside a space's operating hours.
 */
public class OutsideOperatingHoursException extends RuntimeException {

    /**
     * Constructs a new OutsideOperatingHoursException with time details.
     *
     * @param requestedStart the requested start time
     * @param requestedEnd the requested end time
     * @param operatingStart the space's operating start time
     * @param operatingEnd the space's operating end time
     */
    public OutsideOperatingHoursException(LocalTime requestedStart, LocalTime requestedEnd,
                                          LocalTime operatingStart, LocalTime operatingEnd) {
        super(String.format(
            "Reservation time %s-%s is outside operating hours %s-%s",
            requestedStart, requestedEnd, operatingStart, operatingEnd));
    }
}
