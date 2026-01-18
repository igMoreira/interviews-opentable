package com.opentable.privatedining.exception;

import org.bson.types.ObjectId;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Exception thrown when a reservation conflicts with an existing reservation for the same space.
 */
public class ReservationConflictException extends RuntimeException {

    /**
     * Constructs a new ReservationConflictException with conflict details.
     *
     * @param restaurantId the restaurant ID
     * @param spaceId the space UUID
     * @param startTime the conflicting start time
     * @param endTime the conflicting end time
     */
    public ReservationConflictException(ObjectId restaurantId, UUID spaceId, LocalDateTime startTime, LocalDateTime endTime) {
        super("Reservation conflict: the requested time slot (" + startTime + " to " + endTime +
              ") overlaps with an existing reservation for space " + spaceId + " in restaurant " + restaurantId);
    }

    /**
     * Constructs a new ReservationConflictException with a custom message.
     *
     * @param message the detail message
     */
    public ReservationConflictException(String message) {
        super(message);
    }
}