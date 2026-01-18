package com.opentable.privatedining.service;

import com.opentable.privatedining.config.AnalyticsConfig;
import com.opentable.privatedining.dto.OccupancyReportResponse;
import com.opentable.privatedining.dto.SpaceOccupancyReport;
import com.opentable.privatedining.dto.TimeSlotOccupancy;
import com.opentable.privatedining.exception.InvalidDateRangeException;
import com.opentable.privatedining.exception.RestaurantNotFoundException;
import com.opentable.privatedining.exception.SpaceNotFoundException;
import com.opentable.privatedining.model.Reservation;
import com.opentable.privatedining.model.Restaurant;
import com.opentable.privatedining.model.Space;
import com.opentable.privatedining.repository.ReservationRepository;
import com.opentable.privatedining.repository.RestaurantRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OccupancyAnalyticsServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private AnalyticsConfig analyticsConfig;

    @InjectMocks
    private OccupancyAnalyticsService occupancyAnalyticsService;

    private ObjectId restaurantId;
    private Restaurant restaurant;
    private Space space1;
    private Space space2;

    @BeforeEach
    void setUp() {
        restaurantId = new ObjectId();

        space1 = new Space("Garden Room", 5, 25);
        space1.setId(UUID.randomUUID());

        space2 = new Space("Wine Cellar", 2, 15);
        space2.setId(UUID.randomUUID());

        restaurant = new Restaurant("Test Restaurant", "123 Main St", "Italian", 100);
        restaurant.setId(restaurantId);
        restaurant.setSpaces(Arrays.asList(space1, space2));
    }

    // ==================== Date Range Validation Tests ====================

    @Test
    void generateOccupancyReport_WhenStartTimeIsNull_ShouldThrowException() {
        // Given
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 18, 0);

        // When & Then
        InvalidDateRangeException exception = assertThrows(InvalidDateRangeException.class,
                () -> occupancyAnalyticsService.generateOccupancyReport(
                        restaurantId, null, endTime, null, 0, 10));

        assertTrue(exception.getMessage().contains("Start time and end time are required"));
    }

    @Test
    void generateOccupancyReport_WhenEndTimeIsNull_ShouldThrowException() {
        // Given
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 9, 0);

        // When & Then
        InvalidDateRangeException exception = assertThrows(InvalidDateRangeException.class,
                () -> occupancyAnalyticsService.generateOccupancyReport(
                        restaurantId, startTime, null, null, 0, 10));

        assertTrue(exception.getMessage().contains("Start time and end time are required"));
    }

    @Test
    void generateOccupancyReport_WhenBothTimesAreNull_ShouldThrowException() {
        // When & Then
        InvalidDateRangeException exception = assertThrows(InvalidDateRangeException.class,
                () -> occupancyAnalyticsService.generateOccupancyReport(
                        restaurantId, null, null, null, 0, 10));

        assertTrue(exception.getMessage().contains("Start time and end time are required"));
    }

    @Test
    void generateOccupancyReport_WhenEndTimeBeforeStartTime_ShouldThrowException() {
        // Given
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 18, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 9, 0);

        // When & Then
        InvalidDateRangeException exception = assertThrows(InvalidDateRangeException.class,
                () -> occupancyAnalyticsService.generateOccupancyReport(
                        restaurantId, startTime, endTime, null, 0, 10));

        assertTrue(exception.getMessage().contains("End time must be after start time"));
    }

    @Test
    void generateOccupancyReport_WhenRangeExceeds31Days_ShouldThrowException() {
        // Given
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 1, 9, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 2, 5, 18, 0); // 35 days

        when(analyticsConfig.getMaxRangeDays()).thenReturn(31);

        // When & Then
        InvalidDateRangeException exception = assertThrows(InvalidDateRangeException.class,
                () -> occupancyAnalyticsService.generateOccupancyReport(
                        restaurantId, startTime, endTime, null, 0, 10));

        assertTrue(exception.getMessage().contains("exceeds maximum allowed range of 31 days"));
    }

    @Test
    void generateOccupancyReport_WhenRangeExactly31Days_ShouldPass() {
        // Given
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 1, 9, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 2, 1, 9, 0); // exactly 31 days

        when(analyticsConfig.getMaxRangeDays()).thenReturn(31);
        when(analyticsConfig.getTimeSlotDurationMinutes()).thenReturn(60);
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reservationRepository.findByRestaurantIdAndTimeRange(eq(restaurantId), any(), any()))
                .thenReturn(Collections.emptyList());

        // When & Then - should not throw
        assertDoesNotThrow(() -> occupancyAnalyticsService.generateOccupancyReport(
                restaurantId, startTime, endTime, null, 0, 10));
    }

    // ==================== Restaurant/Space Not Found Tests ====================

    @Test
    void generateOccupancyReport_WhenRestaurantNotFound_ShouldThrowException() {
        // Given
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 9, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 18, 0);

        when(analyticsConfig.getMaxRangeDays()).thenReturn(31);
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RestaurantNotFoundException.class,
                () -> occupancyAnalyticsService.generateOccupancyReport(
                        restaurantId, startTime, endTime, null, 0, 10));
    }

    @Test
    void generateOccupancyReport_WhenSpaceNotFound_ShouldThrowException() {
        // Given
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 9, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 18, 0);
        UUID nonExistentSpaceId = UUID.randomUUID();

        when(analyticsConfig.getMaxRangeDays()).thenReturn(31);
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));

        // When & Then
        assertThrows(SpaceNotFoundException.class,
                () -> occupancyAnalyticsService.generateOccupancyReport(
                        restaurantId, startTime, endTime, nonExistentSpaceId, 0, 10));
    }

    // ==================== Space Filtering Tests ====================

    @Test
    void generateOccupancyReport_WithSpaceIdFilter_ShouldReturnOnlyThatSpace() {
        // Given
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 9, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 12, 0);

        when(analyticsConfig.getMaxRangeDays()).thenReturn(31);
        when(analyticsConfig.getTimeSlotDurationMinutes()).thenReturn(60);
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reservationRepository.findByRestaurantIdAndSpaceIdAndTimeRange(
                eq(restaurantId), eq(space1.getId()), any(), any()))
                .thenReturn(Collections.emptyList());

        // When
        OccupancyReportResponse response = occupancyAnalyticsService.generateOccupancyReport(
                restaurantId, startTime, endTime, space1.getId(), 0, 10);

        // Then
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getSpaceReports().size());
        assertEquals(space1.getId(), response.getSpaceReports().get(0).getSpaceId());
    }

    @Test
    void generateOccupancyReport_WithoutSpaceIdFilter_ShouldReturnAllSpaces() {
        // Given
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 9, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 12, 0);

        when(analyticsConfig.getMaxRangeDays()).thenReturn(31);
        when(analyticsConfig.getTimeSlotDurationMinutes()).thenReturn(60);
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reservationRepository.findByRestaurantIdAndTimeRange(eq(restaurantId), any(), any()))
                .thenReturn(Collections.emptyList());

        // When
        OccupancyReportResponse response = occupancyAnalyticsService.generateOccupancyReport(
                restaurantId, startTime, endTime, null, 0, 10);

        // Then
        assertEquals(2, response.getTotalElements());
        assertEquals(2, response.getSpaceReports().size());
    }

    // ==================== Pagination Tests ====================

    @Test
    void generateOccupancyReport_WithPagination_ShouldReturnCorrectPage() {
        // Given
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 9, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 12, 0);

        when(analyticsConfig.getMaxRangeDays()).thenReturn(31);
        when(analyticsConfig.getTimeSlotDurationMinutes()).thenReturn(60);
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reservationRepository.findByRestaurantIdAndTimeRange(eq(restaurantId), any(), any()))
                .thenReturn(Collections.emptyList());

        // When - request page 0 with size 1
        OccupancyReportResponse response = occupancyAnalyticsService.generateOccupancyReport(
                restaurantId, startTime, endTime, null, 0, 1);

        // Then
        assertEquals(0, response.getPage());
        assertEquals(1, response.getSize());
        assertEquals(2, response.getTotalElements());
        assertEquals(2, response.getTotalPages());
        assertEquals(1, response.getSpaceReports().size());
    }

    @Test
    void generateOccupancyReport_WithPagination_SecondPage_ShouldReturnCorrectSpace() {
        // Given
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 9, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 12, 0);

        when(analyticsConfig.getMaxRangeDays()).thenReturn(31);
        when(analyticsConfig.getTimeSlotDurationMinutes()).thenReturn(60);
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reservationRepository.findByRestaurantIdAndTimeRange(eq(restaurantId), any(), any()))
                .thenReturn(Collections.emptyList());

        // When - request page 1 with size 1
        OccupancyReportResponse response = occupancyAnalyticsService.generateOccupancyReport(
                restaurantId, startTime, endTime, null, 1, 1);

        // Then
        assertEquals(1, response.getPage());
        assertEquals(1, response.getSpaceReports().size());
        assertEquals(space2.getId(), response.getSpaceReports().get(0).getSpaceId());
    }

    @Test
    void generateOccupancyReport_WithPaginationBeyondTotal_ShouldReturnEmptyResults() {
        // Given - page beyond available data
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 9, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 12, 0);

        when(analyticsConfig.getMaxRangeDays()).thenReturn(31);
        when(analyticsConfig.getTimeSlotDurationMinutes()).thenReturn(60);
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reservationRepository.findByRestaurantIdAndTimeRange(eq(restaurantId), any(), any()))
                .thenReturn(Collections.emptyList());

        // When - request page 10 which doesn't exist (only 2 spaces)
        OccupancyReportResponse response = occupancyAnalyticsService.generateOccupancyReport(
                restaurantId, startTime, endTime, null, 10, 10);

        // Then
        assertEquals(10, response.getPage());
        assertEquals(0, response.getSpaceReports().size());
        assertEquals(2, response.getTotalElements());
    }

    // ==================== Occupancy Calculation Tests ====================

    @Test
    void generateOccupancyReport_WithReservations_ShouldCalculateCorrectOccupancy() {
        // Given
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 13, 0);

        Reservation reservation1 = createReservation(restaurantId, space1.getId(), 8,
                LocalDateTime.of(2026, 1, 20, 10, 0),
                LocalDateTime.of(2026, 1, 20, 12, 0));

        Reservation reservation2 = createReservation(restaurantId, space1.getId(), 6,
                LocalDateTime.of(2026, 1, 20, 11, 0),
                LocalDateTime.of(2026, 1, 20, 13, 0));

        when(analyticsConfig.getMaxRangeDays()).thenReturn(31);
        when(analyticsConfig.getTimeSlotDurationMinutes()).thenReturn(60);
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reservationRepository.findByRestaurantIdAndSpaceIdAndTimeRange(
                eq(restaurantId), eq(space1.getId()), any(), any()))
                .thenReturn(Arrays.asList(reservation1, reservation2));

        // When
        OccupancyReportResponse response = occupancyAnalyticsService.generateOccupancyReport(
                restaurantId, startTime, endTime, space1.getId(), 0, 10);

        // Then
        SpaceOccupancyReport spaceReport = response.getSpaceReports().get(0);
        assertEquals(2, spaceReport.getTotalReservations());

        // Check hourly breakdown
        List<TimeSlotOccupancy> hourlyBreakdown = spaceReport.getHourlyBreakdown();
        assertEquals(3, hourlyBreakdown.size()); // 10-11, 11-12, 12-13

        // 10:00-11:00: reservation1 only (8 guests)
        TimeSlotOccupancy slot1 = hourlyBreakdown.get(0);
        assertEquals(1, slot1.getReservationCount());
        assertEquals(8, slot1.getOccupancy());

        // 11:00-12:00: both reservations (8 + 6 = 14 guests)
        TimeSlotOccupancy slot2 = hourlyBreakdown.get(1);
        assertEquals(2, slot2.getReservationCount());
        assertEquals(14, slot2.getOccupancy());

        // 12:00-13:00: reservation2 only (6 guests)
        TimeSlotOccupancy slot3 = hourlyBreakdown.get(2);
        assertEquals(1, slot3.getReservationCount());
        assertEquals(6, slot3.getOccupancy());

        // Peak occupancy should be 14
        assertEquals(14, spaceReport.getPeakOccupancy());
    }

    @Test
    void generateOccupancyReport_ShouldCalculateCorrectUtilization() {
        // Given
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 11, 0);

        // Create reservation with 10 guests in space with max capacity 25 = 40% utilization
        Reservation reservation = createReservation(restaurantId, space1.getId(), 10,
                LocalDateTime.of(2026, 1, 20, 10, 0),
                LocalDateTime.of(2026, 1, 20, 11, 0));

        when(analyticsConfig.getMaxRangeDays()).thenReturn(31);
        when(analyticsConfig.getTimeSlotDurationMinutes()).thenReturn(60);
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reservationRepository.findByRestaurantIdAndSpaceIdAndTimeRange(
                eq(restaurantId), eq(space1.getId()), any(), any()))
                .thenReturn(List.of(reservation));

        // When
        OccupancyReportResponse response = occupancyAnalyticsService.generateOccupancyReport(
                restaurantId, startTime, endTime, space1.getId(), 0, 10);

        // Then
        SpaceOccupancyReport spaceReport = response.getSpaceReports().get(0);
        TimeSlotOccupancy slot = spaceReport.getHourlyBreakdown().get(0);

        assertEquals(10, slot.getOccupancy());
        assertEquals(25, slot.getMaxCapacity());
        assertEquals(40.0, slot.getUtilizationPercentage(), 0.01);
    }

    @Test
    void generateOccupancyReport_WithNoReservations_ShouldReturnEmptyMetrics() {
        // Given
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 9, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 12, 0);

        when(analyticsConfig.getMaxRangeDays()).thenReturn(31);
        when(analyticsConfig.getTimeSlotDurationMinutes()).thenReturn(60);
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reservationRepository.findByRestaurantIdAndTimeRange(eq(restaurantId), any(), any()))
                .thenReturn(Collections.emptyList());

        // When
        OccupancyReportResponse response = occupancyAnalyticsService.generateOccupancyReport(
                restaurantId, startTime, endTime, null, 0, 10);

        // Then
        assertEquals(0, response.getSummary().getTotalReservations());
        assertEquals(0, response.getSummary().getTotalGuests());
        assertEquals(0, response.getSummary().getPeakOccupancy());
        assertEquals(0.0, response.getSummary().getAverageUtilization(), 0.01);
    }

    @Test
    void generateOccupancyReport_ShouldCalculateCorrectSummary() {
        // Given
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 12, 0);

        Reservation res1 = createReservation(restaurantId, space1.getId(), 10,
                LocalDateTime.of(2026, 1, 20, 10, 0),
                LocalDateTime.of(2026, 1, 20, 11, 0));

        Reservation res2 = createReservation(restaurantId, space2.getId(), 5,
                LocalDateTime.of(2026, 1, 20, 11, 0),
                LocalDateTime.of(2026, 1, 20, 12, 0));

        when(analyticsConfig.getMaxRangeDays()).thenReturn(31);
        when(analyticsConfig.getTimeSlotDurationMinutes()).thenReturn(60);
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reservationRepository.findByRestaurantIdAndTimeRange(eq(restaurantId), any(), any()))
                .thenReturn(Arrays.asList(res1, res2));

        // When
        OccupancyReportResponse response = occupancyAnalyticsService.generateOccupancyReport(
                restaurantId, startTime, endTime, null, 0, 10);

        // Then
        assertEquals(2, response.getSummary().getTotalReservations());
        assertEquals(15, response.getSummary().getTotalGuests()); // 10 + 5
    }

    @Test
    void generateOccupancyReport_WithZeroCapacitySpace_ShouldHandleGracefully() {
        // Given - edge case where space has zero capacity (utilization calculation)
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 10, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 11, 0);

        // Create a space with zero max capacity
        Space zeroCapacitySpace = new Space("Zero Room", 0, 0);
        zeroCapacitySpace.setId(UUID.randomUUID());

        Restaurant testRestaurant = new Restaurant("Test Restaurant", "123 Main St", "Italian", 100);
        testRestaurant.setId(restaurantId);
        testRestaurant.setSpaces(List.of(zeroCapacitySpace));

        when(analyticsConfig.getMaxRangeDays()).thenReturn(31);
        when(analyticsConfig.getTimeSlotDurationMinutes()).thenReturn(60);
        when(restaurantRepository.findById(restaurantId)).thenReturn(Optional.of(testRestaurant));
        when(reservationRepository.findByRestaurantIdAndTimeRange(eq(restaurantId), any(), any()))
                .thenReturn(Collections.emptyList());

        // When
        OccupancyReportResponse response = occupancyAnalyticsService.generateOccupancyReport(
                restaurantId, startTime, endTime, null, 0, 10);

        // Then - should not throw division by zero, utilization should be 0
        assertNotNull(response);
        assertEquals(1, response.getSpaceReports().size());
        SpaceOccupancyReport spaceReport = response.getSpaceReports().get(0);
        assertEquals(0.0, spaceReport.getAverageUtilization(), 0.01);
    }

    // ==================== Helper Methods ====================

    private Reservation createReservation(ObjectId restaurantId, UUID spaceId, int partySize,
                                          LocalDateTime startTime, LocalDateTime endTime) {
        Reservation reservation = new Reservation();
        reservation.setId(new ObjectId());
        reservation.setRestaurantId(restaurantId);
        reservation.setSpaceId(spaceId);
        reservation.setPartySize(partySize);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);
        reservation.setCustomerEmail("test@example.com");
        reservation.setStatus("CONFIRMED");
        return reservation;
    }
}

