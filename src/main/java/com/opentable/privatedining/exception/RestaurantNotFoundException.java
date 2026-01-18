package com.opentable.privatedining.exception;

import org.bson.types.ObjectId;

/**
 * Exception thrown when a restaurant is not found by its ID.
 */
public class RestaurantNotFoundException extends RuntimeException {

    /**
     * Constructs a new RestaurantNotFoundException with the restaurant ID.
     *
     * @param restaurantId the ID of the restaurant that was not found
     */
    public RestaurantNotFoundException(ObjectId restaurantId) {
        super("Restaurant not found with ID: " + restaurantId.toString());
    }

    /**
     * Constructs a new RestaurantNotFoundException with a custom message.
     *
     * @param message the detail message
     */
    public RestaurantNotFoundException(String message) {
        super(message);
    }
}