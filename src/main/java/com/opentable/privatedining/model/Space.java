package com.opentable.privatedining.model;

import com.opentable.privatedining.config.SpaceDefaultsConfig;

import java.time.LocalTime;
import java.util.UUID;

public class Space {

    private UUID id;
    private String name;
    private Integer minCapacity;
    private Integer maxCapacity;
    private LocalTime operatingStartTime;
    private LocalTime operatingEndTime;
    private Integer timeSlotDurationMinutes;

    public Space() {
        this.id = UUID.randomUUID();
    }

    public Space(String name, Integer minCapacity, Integer maxCapacity) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.minCapacity = minCapacity;
        this.maxCapacity = maxCapacity;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getMinCapacity() {
        return minCapacity;
    }

    public void setMinCapacity(Integer minCapacity) {
        this.minCapacity = minCapacity;
    }

    public Integer getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(Integer maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public LocalTime getOperatingStartTime() {
        if (operatingStartTime != null) {
            return operatingStartTime;
        }
        return SpaceDefaultsConfig.getInstance().getOperatingStartTime();
    }

    public void setOperatingStartTime(LocalTime operatingStartTime) {
        this.operatingStartTime = operatingStartTime;
    }

    public LocalTime getOperatingEndTime() {
        if (operatingEndTime != null) {
            return operatingEndTime;
        }
        return SpaceDefaultsConfig.getInstance().getOperatingEndTime();
    }

    public void setOperatingEndTime(LocalTime operatingEndTime) {
        this.operatingEndTime = operatingEndTime;
    }

    public Integer getTimeSlotDurationMinutes() {
        if (timeSlotDurationMinutes != null) {
            return timeSlotDurationMinutes;
        }
        return SpaceDefaultsConfig.getInstance().getTimeSlotDurationMinutes();
    }

    public void setTimeSlotDurationMinutes(Integer timeSlotDurationMinutes) {
        this.timeSlotDurationMinutes = timeSlotDurationMinutes;
    }
}


