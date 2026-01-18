package com.opentable.privatedining.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuration class for caching.
 * Configures Caffeine cache for occupancy analytics reports.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String OCCUPANCY_REPORTS_CACHE = "occupancyReports";

    private final AnalyticsConfig analyticsConfig;

    public CacheConfig(AnalyticsConfig analyticsConfig) {
        this.analyticsConfig = analyticsConfig;
    }

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(OCCUPANCY_REPORTS_CACHE);
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(analyticsConfig.getCacheTtlMinutes(), TimeUnit.MINUTES)
                .maximumSize(analyticsConfig.getCacheMaxSize()));
        return cacheManager;
    }
}

