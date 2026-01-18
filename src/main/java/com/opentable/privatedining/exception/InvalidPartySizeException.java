package com.opentable.privatedining.exception;

/**
 * Exception thrown when a reservation party size is outside the space's capacity range.
 */
public class InvalidPartySizeException extends RuntimeException {

    /**
     * Constructs a new InvalidPartySizeException with capacity range details.
     *
     * @param partySize the requested party size
     * @param minCapacity the minimum capacity of the space
     * @param maxCapacity the maximum capacity of the space
     */
    public InvalidPartySizeException(int partySize, int minCapacity, int maxCapacity) {
        super("Party size " + partySize + " is outside the space capacity range of " + minCapacity + "-" + maxCapacity);
    }

    /**
     * Constructs a new InvalidPartySizeException with a custom message.
     *
     * @param message the detail message
     */
    public InvalidPartySizeException(String message) {
        super(message);
    }
}