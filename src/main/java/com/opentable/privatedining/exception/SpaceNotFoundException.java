package com.opentable.privatedining.exception;

import org.bson.types.ObjectId;
import java.util.UUID;

/**
 * Exception thrown when a space is not found by its ID.
 */
public class SpaceNotFoundException extends RuntimeException {

    /**
     * Constructs a new SpaceNotFoundException with restaurant and space IDs.
     *
     * @param restaurantId the restaurant ID containing the space
     * @param spaceId the UUID of the space that was not found
     */
    public SpaceNotFoundException(ObjectId restaurantId, UUID spaceId) {
        super("Space not found with ID: " + spaceId.toString() + " in restaurant: " + restaurantId.toString());
    }

    /**
     * Constructs a new SpaceNotFoundException with just the space ID.
     *
     * @param spaceId the UUID of the space that was not found
     */
    public SpaceNotFoundException(UUID spaceId) {
        super("Space not found with ID: " + spaceId.toString());
    }

    /**
     * Constructs a new SpaceNotFoundException with a custom message.
     *
     * @param message the detail message
     */
    public SpaceNotFoundException(String message) {
        super(message);
    }
}