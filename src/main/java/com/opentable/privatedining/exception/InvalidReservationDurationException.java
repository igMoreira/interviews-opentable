package com.opentable.privatedining.exception;

public class InvalidReservationDurationException extends RuntimeException {

    public InvalidReservationDurationException(int slotDurationMinutes) {
        super("Reservation duration must be at least " + slotDurationMinutes + " minutes");
    }

    public InvalidReservationDurationException(String message) {
        super(message);
    }
}

