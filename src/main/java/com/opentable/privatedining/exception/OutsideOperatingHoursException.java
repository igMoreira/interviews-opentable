package com.opentable.privatedining.exception;

import java.time.LocalTime;

public class OutsideOperatingHoursException extends RuntimeException {

    public OutsideOperatingHoursException(LocalTime requestedStart, LocalTime requestedEnd,
                                          LocalTime operatingStart, LocalTime operatingEnd) {
        super(String.format(
            "Reservation time %s-%s is outside operating hours %s-%s",
            requestedStart, requestedEnd, operatingStart, operatingEnd));
    }
}

