package com.opentable.privatedining.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "private-dining.analytics")
public class AnalyticsConfig {

    private Integer timeSlotDurationMinutes = 60;
    private Integer maxRangeDays = 31;
}

