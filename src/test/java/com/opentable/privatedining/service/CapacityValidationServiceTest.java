package com.opentable.privatedining.service;

import com.opentable.privatedining.exception.CapacityExceededException;
import com.opentable.privatedining.model.Reservation;
import com.opentable.privatedining.model.Space;
import com.opentable.privatedining.repository.ReservationRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CapacityValidationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private CapacityValidationService capacityValidationService;

    // ==================== validateCapacity Tests ====================

    @Test
    void validateCapacity_WhenNoExistingReservations_ShouldPass() {
        // Given
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();

        Reservation newReservation = createReservation(restaurantId, spaceId, 4,
            LocalDateTime.of(2026, 1, 20, 12, 0),
            LocalDateTime.of(2026, 1, 20, 14, 0));

        Space space = new Space("Test Space", 2, 10);
        space.setId(spaceId);

        when(reservationRepository.findOverlappingReservations(
            restaurantId, spaceId, newReservation.getStartTime(), newReservation.getEndTime()))
            .thenReturn(Collections.emptyList());

        // When & Then - no exception should be thrown
        assertDoesNotThrow(() -> capacityValidationService.validateCapacity(newReservation, space));
    }

    @Test
    void validateCapacity_WhenConcurrentReservationsWithinCapacity_ShouldPass() {
        // Given: Existing reservation with 4 people, new reservation with 4 people, max capacity 10
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();

        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 12, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 14, 0);

        Reservation existingReservation = createReservation(restaurantId, spaceId, 4, startTime, endTime);
        existingReservation.setId(new ObjectId());

        Reservation newReservation = createReservation(restaurantId, spaceId, 4, startTime, endTime);

        Space space = new Space("Test Space", 2, 10);
        space.setId(spaceId);

        when(reservationRepository.findOverlappingReservations(restaurantId, spaceId, startTime, endTime))
            .thenReturn(List.of(existingReservation));

        // When & Then - 4 + 4 = 8 <= 10, should pass
        assertDoesNotThrow(() -> capacityValidationService.validateCapacity(newReservation, space));
    }

    @Test
    void validateCapacity_WhenAtExactCapacityLimit_ShouldPass() {
        // Given: Existing reservation with 6 people, new reservation with 4 people, max capacity 10
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();

        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 12, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 14, 0);

        Reservation existingReservation = createReservation(restaurantId, spaceId, 6, startTime, endTime);
        existingReservation.setId(new ObjectId());

        Reservation newReservation = createReservation(restaurantId, spaceId, 4, startTime, endTime);

        Space space = new Space("Test Space", 2, 10);
        space.setId(spaceId);

        when(reservationRepository.findOverlappingReservations(restaurantId, spaceId, startTime, endTime))
            .thenReturn(List.of(existingReservation));

        // When & Then - 6 + 4 = 10 == 10, should pass (exactly at limit)
        assertDoesNotThrow(() -> capacityValidationService.validateCapacity(newReservation, space));
    }

    @Test
    void validateCapacity_WhenCombinedHeadcountExceedsCapacity_ShouldThrowException() {
        // Given: Existing reservation with 6 people, new reservation with 5 people, max capacity 10
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();

        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 12, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 14, 0);

        Reservation existingReservation = createReservation(restaurantId, spaceId, 6, startTime, endTime);
        existingReservation.setId(new ObjectId());

        Reservation newReservation = createReservation(restaurantId, spaceId, 5, startTime, endTime);

        Space space = new Space("Test Space", 2, 10);
        space.setId(spaceId);

        when(reservationRepository.findOverlappingReservations(restaurantId, spaceId, startTime, endTime))
            .thenReturn(List.of(existingReservation));

        // When & Then - 6 + 5 = 11 > 10, should fail
        CapacityExceededException exception = assertThrows(CapacityExceededException.class, () ->
            capacityValidationService.validateCapacity(newReservation, space));

        assertTrue(exception.getMessage().contains("Cannot accommodate party of 5"));
        assertTrue(exception.getMessage().contains("Current occupancy: 6"));
        assertTrue(exception.getMessage().contains("Max capacity: 10"));
        assertTrue(exception.getMessage().contains("Available: 4"));
    }

    @Test
    void validateCapacity_WhenMultipleExistingReservations_ShouldSumAllPartySizes() {
        // Given: Multiple existing reservations totaling 7 people, new reservation with 4 people
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();

        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 12, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 14, 0);

        Reservation existing1 = createReservation(restaurantId, spaceId, 3, startTime, endTime);
        existing1.setId(new ObjectId());
        Reservation existing2 = createReservation(restaurantId, spaceId, 4, startTime, endTime);
        existing2.setId(new ObjectId());

        Reservation newReservation = createReservation(restaurantId, spaceId, 4, startTime, endTime);

        Space space = new Space("Test Space", 2, 10);
        space.setId(spaceId);

        when(reservationRepository.findOverlappingReservations(restaurantId, spaceId, startTime, endTime))
            .thenReturn(Arrays.asList(existing1, existing2));

        // When & Then - 3 + 4 + 4 = 11 > 10, should fail
        CapacityExceededException exception = assertThrows(CapacityExceededException.class, () ->
            capacityValidationService.validateCapacity(newReservation, space));

        assertTrue(exception.getMessage().contains("Current occupancy: 7"));
    }

    @Test
    void validateCapacity_WhenOnePersonOverCapacity_ShouldThrowException() {
        // Given: Current occupancy 9, new reservation 2 people, max 10
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();

        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 12, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 14, 0);

        Reservation existingReservation = createReservation(restaurantId, spaceId, 9, startTime, endTime);
        existingReservation.setId(new ObjectId());

        Reservation newReservation = createReservation(restaurantId, spaceId, 2, startTime, endTime);

        Space space = new Space("Test Space", 2, 10);
        space.setId(spaceId);

        when(reservationRepository.findOverlappingReservations(restaurantId, spaceId, startTime, endTime))
            .thenReturn(List.of(existingReservation));

        // When & Then - 9 + 2 = 11 > 10 (just 1 over)
        assertThrows(CapacityExceededException.class, () ->
            capacityValidationService.validateCapacity(newReservation, space));
    }

    // ==================== validateCapacityExcluding Tests ====================

    @Test
    void validateCapacityExcluding_ShouldExcludeSpecifiedReservation() {
        // Given: Updating existing reservation, should exclude itself from calculation
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();
        ObjectId excludeId = new ObjectId();

        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 12, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 14, 0);

        Reservation existingToExclude = createReservation(restaurantId, spaceId, 5, startTime, endTime);
        existingToExclude.setId(excludeId);

        Reservation otherExisting = createReservation(restaurantId, spaceId, 3, startTime, endTime);
        otherExisting.setId(new ObjectId());

        Reservation updatedReservation = createReservation(restaurantId, spaceId, 6, startTime, endTime);
        updatedReservation.setId(excludeId);

        Space space = new Space("Test Space", 2, 10);
        space.setId(spaceId);

        when(reservationRepository.findOverlappingReservations(restaurantId, spaceId, startTime, endTime))
            .thenReturn(Arrays.asList(existingToExclude, otherExisting));

        // When & Then - Only otherExisting (3) + updated (6) = 9 <= 10, should pass
        assertDoesNotThrow(() ->
            capacityValidationService.validateCapacityExcluding(updatedReservation, space, excludeId));
    }

    @Test
    void validateCapacityExcluding_WhenExceedsCapacity_ShouldThrowException() {
        // Given: Updating reservation would exceed capacity even when excluding itself
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();
        ObjectId excludeId = new ObjectId();

        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 12, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 14, 0);

        Reservation existingToExclude = createReservation(restaurantId, spaceId, 3, startTime, endTime);
        existingToExclude.setId(excludeId);

        Reservation otherExisting = createReservation(restaurantId, spaceId, 7, startTime, endTime);
        otherExisting.setId(new ObjectId());

        // Try to update to 5 people: otherExisting (7) + updated (5) = 12 > 10
        Reservation updatedReservation = createReservation(restaurantId, spaceId, 5, startTime, endTime);
        updatedReservation.setId(excludeId);

        Space space = new Space("Test Space", 2, 10);
        space.setId(spaceId);

        when(reservationRepository.findOverlappingReservations(restaurantId, spaceId, startTime, endTime))
            .thenReturn(Arrays.asList(existingToExclude, otherExisting));

        // When & Then - 7 + 5 = 12 > 10, should fail
        CapacityExceededException exception = assertThrows(CapacityExceededException.class, () ->
            capacityValidationService.validateCapacityExcluding(updatedReservation, space, excludeId));

        assertTrue(exception.getMessage().contains("Cannot accommodate party of 5"));
        assertTrue(exception.getMessage().contains("Current occupancy: 7"));
    }

    // ==================== calculateCurrentOccupancy Tests ====================

    @Test
    void calculateCurrentOccupancy_WhenNoReservations_ShouldReturnZero() {
        // Given
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 12, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 14, 0);

        when(reservationRepository.findOverlappingReservations(restaurantId, spaceId, startTime, endTime))
            .thenReturn(Collections.emptyList());

        // When
        int result = capacityValidationService.calculateCurrentOccupancy(
            restaurantId, spaceId, startTime, endTime);

        // Then
        assertEquals(0, result);
    }

    @Test
    void calculateCurrentOccupancy_ShouldSumAllOverlappingReservations() {
        // Given
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 12, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 14, 0);

        Reservation res1 = createReservation(restaurantId, spaceId, 3, startTime, endTime);
        Reservation res2 = createReservation(restaurantId, spaceId, 4, startTime, endTime);
        Reservation res3 = createReservation(restaurantId, spaceId, 2, startTime, endTime);

        when(reservationRepository.findOverlappingReservations(restaurantId, spaceId, startTime, endTime))
            .thenReturn(Arrays.asList(res1, res2, res3));

        // When
        int result = capacityValidationService.calculateCurrentOccupancy(
            restaurantId, spaceId, startTime, endTime);

        // Then
        assertEquals(9, result); // 3 + 4 + 2 = 9
    }

    // ==================== getAvailableCapacity Tests ====================

    @Test
    void getAvailableCapacity_WhenNoReservations_ShouldReturnFullCapacity() {
        // Given
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 12, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 14, 0);

        when(reservationRepository.findOverlappingReservations(restaurantId, spaceId, startTime, endTime))
            .thenReturn(Collections.emptyList());

        // When
        int result = capacityValidationService.getAvailableCapacity(
            restaurantId, spaceId, startTime, endTime, 10);

        // Then
        assertEquals(10, result);
    }

    @Test
    void getAvailableCapacity_WhenPartiallyBooked_ShouldReturnRemainingCapacity() {
        // Given
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 12, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 14, 0);

        Reservation existing = createReservation(restaurantId, spaceId, 6, startTime, endTime);

        when(reservationRepository.findOverlappingReservations(restaurantId, spaceId, startTime, endTime))
            .thenReturn(List.of(existing));

        // When
        int result = capacityValidationService.getAvailableCapacity(
            restaurantId, spaceId, startTime, endTime, 10);

        // Then
        assertEquals(4, result); // 10 - 6 = 4
    }

    @Test
    void getAvailableCapacity_WhenFullyBooked_ShouldReturnZero() {
        // Given
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 12, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 14, 0);

        Reservation existing = createReservation(restaurantId, spaceId, 10, startTime, endTime);

        when(reservationRepository.findOverlappingReservations(restaurantId, spaceId, startTime, endTime))
            .thenReturn(List.of(existing));

        // When
        int result = capacityValidationService.getAvailableCapacity(
            restaurantId, spaceId, startTime, endTime, 10);

        // Then
        assertEquals(0, result);
    }

    // ==================== Edge Case Tests ====================

    @Test
    void getAvailableCapacity_WhenOccupancyExceedsMaxCapacity_ShouldReturnZero() {
        // Given - edge case where occupancy somehow exceeds max (defensive programming)
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 12, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 14, 0);

        // Create reservations that exceed max capacity (shouldn't happen in reality, but test the defensive code)
        Reservation existing1 = createReservation(restaurantId, spaceId, 8, startTime, endTime);
        Reservation existing2 = createReservation(restaurantId, spaceId, 7, startTime, endTime);

        when(reservationRepository.findOverlappingReservations(restaurantId, spaceId, startTime, endTime))
            .thenReturn(Arrays.asList(existing1, existing2)); // 15 total, exceeds 10

        // When
        int result = capacityValidationService.getAvailableCapacity(
            restaurantId, spaceId, startTime, endTime, 10);

        // Then - should return 0 (Math.max(0, 10-15))
        assertEquals(0, result);
    }

    // ==================== Helper Methods ====================

    private Reservation createReservation(ObjectId restaurantId, UUID spaceId, int partySize,
                                          LocalDateTime startTime, LocalDateTime endTime) {
        Reservation reservation = new Reservation();
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

