package com.opentable.privatedining.exception;

import org.bson.types.ObjectId;

/**
 * Exception thrown when a reservation is not found by its ID.
 */
public class ReservationNotFoundException extends RuntimeException {

    /**
     * Constructs a new ReservationNotFoundException with the reservation ID.
     *
     * @param reservationId the ID of the reservation that was not found
     */
    public ReservationNotFoundException(ObjectId reservationId) {
        super("Reservation not found with ID: " + reservationId.toString());
    }

    /**
     * Constructs a new ReservationNotFoundException with a custom message.
     *
     * @param message the detail message
     */
    public ReservationNotFoundException(String message) {
        super(message);
    }
}