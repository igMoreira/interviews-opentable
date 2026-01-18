package com.opentable.privatedining.exception;

import java.time.LocalDate;

public class MultiDayReservationException extends RuntimeException {

    public MultiDayReservationException(LocalDate startDate, LocalDate endDate) {
        super(String.format(
            "Multi-day reservations are not allowed. Start date: %s, End date: %s",
            startDate, endDate));
    }
}

