package com.opentable.privatedining.model;

import com.opentable.privatedining.config.SpaceDefaultsConfig;
import java.time.LocalTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

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

    public Space() {
        this.id = UUID.randomUUID();
    }

    public Space(String name, Integer minCapacity, Integer maxCapacity) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.minCapacity = minCapacity;
        this.maxCapacity = maxCapacity;
    }

    public LocalTime getOperatingStartTime() {
        if (operatingStartTime != null) {
            return operatingStartTime;
        }
        return SpaceDefaultsConfig.getInstance().getOperatingStartTime();
    }

    public LocalTime getOperatingEndTime() {
        if (operatingEndTime != null) {
            return operatingEndTime;
        }
        return SpaceDefaultsConfig.getInstance().getOperatingEndTime();
    }

    public Integer getTimeSlotDurationMinutes() {
        if (timeSlotDurationMinutes != null) {
            return timeSlotDurationMinutes;
        }
        return SpaceDefaultsConfig.getInstance().getTimeSlotDurationMinutes();
    }
}


