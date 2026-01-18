package com.opentable.privatedining.exception;

import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Exception thrown when a reservation cannot be accommodated because
 * the combined headcount of concurrent reservations would exceed the space's maximum capacity.
 */
public class CapacityExceededException extends RuntimeException {

    public CapacityExceededException(ObjectId restaurantId, UUID spaceId, LocalDateTime startTime,
                                     LocalDateTime endTime, int requestedPartySize,
                                     int currentOccupancy, int maxCapacity) {
        super(String.format(
            "Cannot accommodate party of %d for space %s in restaurant %s from %s to %s. " +
            "Current occupancy: %d, Max capacity: %d, Available: %d",
            requestedPartySize, spaceId, restaurantId, startTime, endTime,
            currentOccupancy, maxCapacity, maxCapacity - currentOccupancy));
    }

    public CapacityExceededException(String message) {
        super(message);
    }
}

