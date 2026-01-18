package com.opentable.privatedining.model;

import com.opentable.privatedining.config.SpaceDefaultsConfig;
import java.time.LocalTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/** Represents a space in the private dining system. */
@AllArgsConstructor
@Getter
@Setter
public class Space {

    private UUID id;
    private String name;
    private Integer minCapacity;
    private Integer maxCapacity;
    @Getter(AccessLevel.NONE)
    private LocalTime operatingStartTime;
    @Getter(AccessLevel.NONE)
    private LocalTime operatingEndTime;
    @Getter(AccessLevel.NONE)
    private Integer timeSlotDurationMinutes;

    /**
     * Default constructor that generates a new UUID for the space.
     */
    public Space() {
        this.id = UUID.randomUUID();
    }

    /**
     * Constructs a new Space with the specified details and generates a new UUID.
     *
     * @param name the name of the space
     * @param minCapacity the minimum capacity of the space
     * @param maxCapacity the maximum capacity of the space
     */
    public Space(String name, Integer minCapacity, Integer maxCapacity) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.minCapacity = minCapacity;
        this.maxCapacity = maxCapacity;
    }

    /**
     * Gets the operating start time for this space.
     * Returns the configured default if not explicitly set.
     *
     * @return the operating start time
     */
    public LocalTime getOperatingStartTime() {
        if (operatingStartTime != null) {
            return operatingStartTime;
        }
        return SpaceDefaultsConfig.getInstance().getOperatingStartTime();
    }

    /**
     * Gets the operating end time for this space.
     * Returns the configured default if not explicitly set.
     *
     * @return the operating end time
     */
    public LocalTime getOperatingEndTime() {
        if (operatingEndTime != null) {
            return operatingEndTime;
        }
        return SpaceDefaultsConfig.getInstance().getOperatingEndTime();
    }

    /**
     * Gets the time slot duration in minutes for this space.
     * Returns the configured default if not explicitly set.
     *
     * @return the time slot duration in minutes
     */
    public Integer getTimeSlotDurationMinutes() {
        if (timeSlotDurationMinutes != null) {
            return timeSlotDurationMinutes;
        }
        return SpaceDefaultsConfig.getInstance().getTimeSlotDurationMinutes();
    }
}
