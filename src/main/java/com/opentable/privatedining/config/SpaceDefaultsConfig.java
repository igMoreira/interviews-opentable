package com.opentable.privatedining.config;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.LocalTime;

@Configuration
@ConfigurationProperties(prefix = "private-dining.space.defaults")
public class SpaceDefaultsConfig {

    private static SpaceDefaultsConfig instance = new SpaceDefaultsConfig();

    private LocalTime operatingStartTime = LocalTime.of(9, 0);
    private LocalTime operatingEndTime = LocalTime.of(22, 0);
    private Integer timeSlotDurationMinutes = 60;

    @PostConstruct
    public void init() {
        instance = this;
    }

    public static SpaceDefaultsConfig getInstance() {
        return instance;
    }

    public LocalTime getOperatingStartTime() {
        return operatingStartTime;
    }

    public void setOperatingStartTime(LocalTime operatingStartTime) {
        this.operatingStartTime = operatingStartTime;
    }

    public LocalTime getOperatingEndTime() {
        return operatingEndTime;
    }

    public void setOperatingEndTime(LocalTime operatingEndTime) {
        this.operatingEndTime = operatingEndTime;
    }

    public Integer getTimeSlotDurationMinutes() {
        return timeSlotDurationMinutes;
    }

    public void setTimeSlotDurationMinutes(Integer timeSlotDurationMinutes) {
        this.timeSlotDurationMinutes = timeSlotDurationMinutes;
    }
}

