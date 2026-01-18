package com.opentable.privatedining.config;

import jakarta.annotation.PostConstruct;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "private-dining.space.defaults")
public class SpaceDefaultsConfig {

    @Getter
    private static SpaceDefaultsConfig instance = new SpaceDefaultsConfig();

    private LocalTime operatingStartTime = LocalTime.of(9, 0);
    private LocalTime operatingEndTime = LocalTime.of(22, 0);
    private Integer timeSlotDurationMinutes = 60;

    @PostConstruct
    public void init() {
        instance = this;
    }
}

