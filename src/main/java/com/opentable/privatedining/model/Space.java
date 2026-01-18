package com.opentable.privatedining.model;

import java.time.LocalTime;
import java.util.UUID;

public class Space {

    private static final LocalTime DEFAULT_OPERATING_START = LocalTime.of(9, 0);
    private static final LocalTime DEFAULT_OPERATING_END = LocalTime.of(22, 0);

    private UUID id;
    private String name;
    private Integer minCapacity;
    private Integer maxCapacity;
    private LocalTime operatingStartTime;
    private LocalTime operatingEndTime;

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
        return operatingStartTime != null ? operatingStartTime : DEFAULT_OPERATING_START;
    }

    public void setOperatingStartTime(LocalTime operatingStartTime) {
        this.operatingStartTime = operatingStartTime;
    }

    public LocalTime getOperatingEndTime() {
        return operatingEndTime != null ? operatingEndTime : DEFAULT_OPERATING_END;
    }

    public void setOperatingEndTime(LocalTime operatingEndTime) {
        this.operatingEndTime = operatingEndTime;
    }
}


