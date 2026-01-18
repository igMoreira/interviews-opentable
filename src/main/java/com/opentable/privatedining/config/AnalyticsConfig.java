package com.opentable.privatedining.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "private-dining.analytics")
public class AnalyticsConfig {

    private Integer timeSlotDurationMinutes = 60;
    private Integer maxRangeDays = 31;

    public Integer getTimeSlotDurationMinutes() {
        return timeSlotDurationMinutes;
    }

    public void setTimeSlotDurationMinutes(Integer timeSlotDurationMinutes) {
        this.timeSlotDurationMinutes = timeSlotDurationMinutes;
    }

    public Integer getMaxRangeDays() {
        return maxRangeDays;
    }

    public void setMaxRangeDays(Integer maxRangeDays) {
        this.maxRangeDays = maxRangeDays;
    }
}

