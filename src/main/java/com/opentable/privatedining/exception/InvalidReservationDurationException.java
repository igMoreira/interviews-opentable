package com.opentable.privatedining.exception;

/**
 * Exception thrown when a reservation duration is less than the minimum time slot duration.
 */
public class InvalidReservationDurationException extends RuntimeException {

    /**
     * Constructs a new InvalidReservationDurationException with slot duration details.
     *
     * @param slotDurationMinutes the minimum slot duration in minutes
     */
    public InvalidReservationDurationException(int slotDurationMinutes) {
        super("Reservation duration must be at least " + slotDurationMinutes + " minutes");
    }

    /**
     * Constructs a new InvalidReservationDurationException with a custom message.
     *
     * @param message the detail message
     */
    public InvalidReservationDurationException(String message) {
        super(message);
    }
}
