package com.opentable.privatedining.exception;

import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Exception thrown when a reservation cannot be accommodated because
 * the combined headcount of concurrent reservations would exceed the space's maximum capacity.
 */
public class CapacityExceededException extends RuntimeException {

    /**
     * Constructs a new CapacityExceededException with detailed capacity information.
     *
     * @param restaurantId the restaurant ID
     * @param spaceId the space UUID
     * @param startTime the start time of the reservation
     * @param endTime the end time of the reservation
     * @param requestedPartySize the requested party size
     * @param currentOccupancy the current occupancy of the space
     * @param maxCapacity the maximum capacity of the space
     */
    public CapacityExceededException(ObjectId restaurantId, UUID spaceId, LocalDateTime startTime,
                                     LocalDateTime endTime, int requestedPartySize,
                                     int currentOccupancy, int maxCapacity) {
        super(String.format(
            "Cannot accommodate party of %d for space %s in restaurant %s from %s to %s. " +
            "Current occupancy: %d, Max capacity: %d, Available: %d",
            requestedPartySize, spaceId, restaurantId, startTime, endTime,
            currentOccupancy, maxCapacity, maxCapacity - currentOccupancy));
    }

    /**
     * Constructs a new CapacityExceededException with a custom message.
     *
     * @param message the detail message
     */
    public CapacityExceededException(String message) {
        super(message);
    }
}
