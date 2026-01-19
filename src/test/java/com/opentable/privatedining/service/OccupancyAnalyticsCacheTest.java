package com.opentable.privatedining.service;

import com.opentable.privatedining.config.CacheConfig;
import com.opentable.privatedining.dto.OccupancyReportDTO;
import com.opentable.privatedining.model.Reservation;
import com.opentable.privatedining.model.Restaurant;
import com.opentable.privatedining.model.Space;
import com.opentable.privatedining.repository.ReservationRepository;
import com.opentable.privatedining.repository.RestaurantRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Integration tests for verifying cache behavior in occupancy analytics.
 * Tests that the cache is properly populated, retrieved, and evicted.
 */
@SpringBootTest(properties = {"springdoc.api-docs.enabled=false", "springdoc.swagger-ui.enabled=false"})
@EnableAutoConfiguration
class OccupancyAnalyticsCacheTest {

    @Autowired
    private OccupancyAnalyticsService occupancyAnalyticsService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private CacheManager cacheManager;

    @MockBean
    private ReservationRepository reservationRepository;

    @MockBean
    private RestaurantRepository restaurantRepository;

    @MockBean
    private RestaurantService restaurantService;

    @MockBean
    private CapacityValidationService capacityValidationService;

    private ObjectId restaurantId;
    private Restaurant restaurant;
    private Space space;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @BeforeEach
    void setUp() {
        // Clear cache before each test
        Objects.requireNonNull(cacheManager.getCache(CacheConfig.OCCUPANCY_REPORTS_CACHE)).clear();

        restaurantId = new ObjectId();
        space = new Space("Test Space", 2, 20);
        space.setId(UUID.randomUUID());

        restaurant = new Restaurant("Test Restaurant", "123 Main St", "Italian", 100);
        restaurant.setId(restaurantId);
        restaurant.setSpaces(Collections.singletonList(space));

        startTime = LocalDateTime.of(2026, 1, 20, 9, 0);
        endTime = LocalDateTime.of(2026, 1, 20, 18, 0);
    }

    @Test
    void generateOccupancyReport_WhenCalledTwice_ShouldUseCacheOnSecondCall() {
        // Given
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reservationRepository.findByRestaurantIdAndTimeRange(eq(restaurantId), any(), any()))
                .thenReturn(Collections.emptyList());

        // When - First call
        OccupancyReportDTO firstResponse = occupancyAnalyticsService.generateOccupancyReport(
                restaurantId, startTime, endTime, null, 0, 10);

        // Second call with same parameters
        OccupancyReportDTO secondResponse = occupancyAnalyticsService.generateOccupancyReport(
                restaurantId, startTime, endTime, null, 0, 10);

        // Then
        assertNotNull(firstResponse);
        assertNotNull(secondResponse);
        assertEquals(firstResponse.getRestaurantId(), secondResponse.getRestaurantId());

        // Repository should only be called once due to caching
        verify(restaurantRepository, times(1)).findById(restaurantId);
        verify(reservationRepository, times(1)).findByRestaurantIdAndTimeRange(eq(restaurantId), any(), any());
    }

    @Test
    void generateOccupancyReport_WhenDifferentParameters_ShouldNotUseCache() {
        // Given
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reservationRepository.findByRestaurantIdAndTimeRange(eq(restaurantId), any(), any()))
                .thenReturn(Collections.emptyList());

        LocalDateTime differentEndTime = LocalDateTime.of(2026, 1, 20, 17, 0);

        // When - First call
        occupancyAnalyticsService.generateOccupancyReport(
                restaurantId, startTime, endTime, null, 0, 10);

        // Second call with different parameters
        occupancyAnalyticsService.generateOccupancyReport(
                restaurantId, startTime, differentEndTime, null, 0, 10);

        // Then - Repository should be called twice (no cache hit)
        verify(restaurantRepository, times(2)).findById(restaurantId);
    }

    @Test
    void createReservation_ShouldEvictCache() {
        // Given
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reservationRepository.findByRestaurantIdAndTimeRange(eq(restaurantId), any(), any()))
                .thenReturn(Collections.emptyList());

        // Populate cache
        occupancyAnalyticsService.generateOccupancyReport(
                restaurantId, startTime, endTime, null, 0, 10);

        // Verify cache is populated
        verify(restaurantRepository, times(1)).findById(restaurantId);

        // Setup for createReservation
        Reservation reservation = new Reservation();
        reservation.setRestaurantId(restaurantId);
        reservation.setSpaceId(space.getId());
        reservation.setCustomerEmail("test@example.com");
        reservation.setStartTime(LocalDateTime.of(2026, 1, 20, 12, 0));
        reservation.setEndTime(LocalDateTime.of(2026, 1, 20, 14, 0));
        reservation.setPartySize(4);
        reservation.setStatus("CONFIRMED");

        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));
        doNothing().when(capacityValidationService).validateCapacity(any(), any());
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        // When - Create a reservation (should evict cache)
        reservationService.createReservation(reservation);

        // Then - Call analytics again
        occupancyAnalyticsService.generateOccupancyReport(
                restaurantId, startTime, endTime, null, 0, 10);

        // Repository should be called again because cache was evicted
        verify(restaurantRepository, times(2)).findById(restaurantId);
    }

    @Test
    void deleteReservation_ShouldEvictCache() {
        // Given
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reservationRepository.findByRestaurantIdAndTimeRange(eq(restaurantId), any(), any()))
                .thenReturn(Collections.emptyList());

        // Populate cache
        occupancyAnalyticsService.generateOccupancyReport(
                restaurantId, startTime, endTime, null, 0, 10);

        // Verify cache is populated
        verify(restaurantRepository, times(1)).findById(restaurantId);

        // Setup for deleteReservation
        ObjectId reservationId = new ObjectId();
        Reservation reservation = new Reservation();
        reservation.setId(reservationId);
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));
        doNothing().when(reservationRepository).deleteById(reservationId);

        // When - Delete a reservation (should evict cache)
        reservationService.deleteReservation(reservationId);

        // Then - Call analytics again
        occupancyAnalyticsService.generateOccupancyReport(
                restaurantId, startTime, endTime, null, 0, 10);

        // Repository should be called again because cache was evicted
        verify(restaurantRepository, times(2)).findById(restaurantId);
    }

    @Test
    void generateOccupancyReport_WithDifferentPagination_ShouldUseSeparateCacheEntries() {
        // Given
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reservationRepository.findByRestaurantIdAndTimeRange(eq(restaurantId), any(), any()))
                .thenReturn(Collections.emptyList());

        // When - First call with page 0
        occupancyAnalyticsService.generateOccupancyReport(
                restaurantId, startTime, endTime, null, 0, 10);

        // Second call with page 1 (different cache key)
        occupancyAnalyticsService.generateOccupancyReport(
                restaurantId, startTime, endTime, null, 1, 10);

        // Then - Repository should be called twice (different cache keys)
        verify(restaurantRepository, times(2)).findById(restaurantId);
    }

    @Test
    void generateOccupancyReport_WithSpaceIdFilter_ShouldUseSeparateCacheEntry() {
        // Given
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reservationRepository.findByRestaurantIdAndTimeRange(eq(restaurantId), any(), any()))
                .thenReturn(Collections.emptyList());
        when(reservationRepository.findByRestaurantIdAndSpaceIdAndTimeRange(eq(restaurantId), eq(space.getId()), any(), any()))
                .thenReturn(Collections.emptyList());

        // When - First call without spaceId
        occupancyAnalyticsService.generateOccupancyReport(
                restaurantId, startTime, endTime, null, 0, 10);

        // Second call with spaceId (different cache key)
        occupancyAnalyticsService.generateOccupancyReport(
                restaurantId, startTime, endTime, space.getId(), 0, 10);

        // Then - Repository should be called twice (different cache keys)
        verify(restaurantRepository, times(2)).findById(restaurantId);
    }
}

