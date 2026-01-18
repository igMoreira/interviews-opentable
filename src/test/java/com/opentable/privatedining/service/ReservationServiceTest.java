package com.opentable.privatedining.service;

import com.opentable.privatedining.exception.InvalidPartySizeException;
import com.opentable.privatedining.exception.InvalidReservationDurationException;
import com.opentable.privatedining.exception.MultiDayReservationException;
import com.opentable.privatedining.exception.OutsideOperatingHoursException;
import com.opentable.privatedining.exception.ReservationConflictException;
import com.opentable.privatedining.exception.ReservationNotFoundException;
import com.opentable.privatedining.exception.RestaurantNotFoundException;
import com.opentable.privatedining.exception.SpaceNotFoundException;
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
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

//TODO: Increase test coverage
@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RestaurantService restaurantService;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    void getAllReservations_ShouldReturnAllReservations() {
        // Given
        Reservation reservation1 = createTestReservation("customer1@example.com", 4);
        Reservation reservation2 = createTestReservation("customer2@example.com", 6);
        List<Reservation> reservations = Arrays.asList(reservation1, reservation2);

        when(reservationRepository.findAll()).thenReturn(reservations);

        // When
        List<Reservation> result = reservationService.getAllReservations();

        // Then
        assertEquals(2, result.size());
        assertEquals("customer1@example.com", result.get(0).getCustomerEmail());
        assertEquals("customer2@example.com", result.get(1).getCustomerEmail());
        verify(reservationRepository).findAll();
    }

    @Test
    void getReservationById_WhenReservationExists_ShouldReturnReservation() {
        // Given
        ObjectId reservationId = new ObjectId();
        Reservation reservation = createTestReservation("test@example.com", 4);
        reservation.setId(reservationId);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        // When
        Optional<Reservation> result = reservationService.getReservationById(reservationId);

        // Then
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getCustomerEmail());
        assertEquals(4, result.get().getPartySize());
        verify(reservationRepository).findById(reservationId);
    }

    @Test
    void getReservationById_WhenReservationNotFound_ShouldReturnEmpty() {
        // Given
        ObjectId reservationId = new ObjectId();
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        // When
        Optional<Reservation> result = reservationService.getReservationById(reservationId);

        // Then
        assertFalse(result.isPresent());
        verify(reservationRepository).findById(reservationId);
    }

    @Test
    void createReservation_WhenValidReservation_ShouldReturnSavedReservation() {
        // Given
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();
        Reservation reservation = createTestReservation("customer@example.com", 4);
        reservation.setRestaurantId(restaurantId);
        reservation.setSpaceId(spaceId);

        com.opentable.privatedining.model.Restaurant restaurant = new com.opentable.privatedining.model.Restaurant("Test Restaurant", "Address", "Cuisine", 50);
        Space space = new Space("Test Space", 2, 8);
        space.setId(spaceId);
        restaurant.setSpaces(List.of(space));

        Reservation savedReservation = createTestReservation("customer@example.com", 4);
        savedReservation.setId(new ObjectId());

        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reservationRepository.findAll()).thenReturn(Arrays.asList());
        when(reservationRepository.save(reservation)).thenReturn(savedReservation);

        // When
        Reservation result = reservationService.createReservation(reservation);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        verify(restaurantService).getRestaurantById(restaurantId);
        verify(reservationRepository).save(reservation);
    }

    @Test
    void createReservation_WhenRestaurantNotFound_ShouldThrowException() {
        // Given
        ObjectId restaurantId = new ObjectId();
        Reservation reservation = createTestReservation("customer@example.com", 4);
        reservation.setRestaurantId(restaurantId);

        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RestaurantNotFoundException.class, () -> {
            reservationService.createReservation(reservation);
        });
        verify(restaurantService).getRestaurantById(restaurantId);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void createReservation_WhenSpaceNotFound_ShouldThrowException() {
        // Given
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();
        Reservation reservation = createTestReservation("customer@example.com", 4);
        reservation.setRestaurantId(restaurantId);
        reservation.setSpaceId(spaceId);

        com.opentable.privatedining.model.Restaurant restaurant = new com.opentable.privatedining.model.Restaurant("Test Restaurant", "Address", "Cuisine", 50);

        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));

        // When & Then
        assertThrows(SpaceNotFoundException.class, () -> {
            reservationService.createReservation(reservation);
        });
        verify(restaurantService).getRestaurantById(restaurantId);
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void createReservation_WhenPartySizeBelowMinCapacity_ShouldThrowException() {
        // Given
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();
        Reservation reservation = createTestReservation("customer@example.com", 1); // Below min capacity
        reservation.setRestaurantId(restaurantId);
        reservation.setSpaceId(spaceId);

        com.opentable.privatedining.model.Restaurant restaurant = new com.opentable.privatedining.model.Restaurant("Test Restaurant", "Address", "Cuisine", 50);
        Space space = new Space("Test Space", 2, 8); // Min capacity is 2
        space.setId(spaceId);
        restaurant.setSpaces(List.of(space));

        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));

        // When & Then
        assertThrows(InvalidPartySizeException.class, () -> {
            reservationService.createReservation(reservation);
        });
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void createReservation_WhenPartySizeAboveMaxCapacity_ShouldThrowException() {
        // Given
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();
        Reservation reservation = createTestReservation("customer@example.com", 10); // Above max capacity
        reservation.setRestaurantId(restaurantId);
        reservation.setSpaceId(spaceId);

        com.opentable.privatedining.model.Restaurant restaurant = new com.opentable.privatedining.model.Restaurant("Test Restaurant", "Address", "Cuisine", 50);
        Space space = new Space("Test Space", 2, 8); // Max capacity is 8
        space.setId(spaceId);
        restaurant.setSpaces(List.of(space));

        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));

        // When & Then
        assertThrows(InvalidPartySizeException.class, () -> {
            reservationService.createReservation(reservation);
        });
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void createReservation_WhenOverlappingReservationExists_ShouldThrowException() {
        // Given
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();

        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.plusHours(2);

        Reservation newReservation = createTestReservation("customer@example.com", 4);
        newReservation.setRestaurantId(restaurantId);
        newReservation.setSpaceId(spaceId);
        newReservation.setStartTime(startTime);
        newReservation.setEndTime(endTime);

        // Existing overlapping reservation
        Reservation existingReservation = createTestReservation("other@example.com", 2);
        existingReservation.setRestaurantId(restaurantId);
        existingReservation.setSpaceId(spaceId);
        existingReservation.setStartTime(startTime.minusMinutes(30));
        existingReservation.setEndTime(endTime.minusMinutes(30));

        com.opentable.privatedining.model.Restaurant restaurant = new com.opentable.privatedining.model.Restaurant("Test Restaurant", "Address", "Cuisine", 50);
        Space space = new Space("Test Space", 2, 8);
        space.setId(spaceId);
        restaurant.setSpaces(List.of(space));

        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reservationRepository.findAll()).thenReturn(Arrays.asList(existingReservation));

        // When & Then
        assertThrows(ReservationConflictException.class, () -> {
            reservationService.createReservation(newReservation);
        });
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void deleteReservation_WhenReservationExists_ShouldReturnTrue() {
        // Given
        ObjectId reservationId = new ObjectId();
        Reservation reservation = createTestReservation("customer@example.com", 4);
        reservation.setId(reservationId);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservation));

        // When
        boolean result = reservationService.deleteReservation(reservationId);

        // Then
        assertTrue(result);
        verify(reservationRepository).findById(reservationId);
        verify(reservationRepository).deleteById(reservationId);
    }

    @Test
    void deleteReservation_WhenReservationNotFound_ShouldReturnFalse() {
        // Given
        ObjectId reservationId = new ObjectId();
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        // When
        boolean result = reservationService.deleteReservation(reservationId);

        // Then
        assertFalse(result);
        verify(reservationRepository).findById(reservationId);
        verify(reservationRepository, never()).deleteById(reservationId);
    }

    @Test
    void getReservationsByRestaurant_ShouldReturnFilteredReservations() {
        // Given
        ObjectId restaurantId = new ObjectId();
        ObjectId otherRestaurantId = new ObjectId();

        Reservation reservation1 = createTestReservation("customer1@example.com", 4);
        reservation1.setRestaurantId(restaurantId);

        Reservation reservation2 = createTestReservation("customer2@example.com", 6);
        reservation2.setRestaurantId(otherRestaurantId);

        Reservation reservation3 = createTestReservation("customer3@example.com", 2);
        reservation3.setRestaurantId(restaurantId);

        List<Reservation> allReservations = Arrays.asList(reservation1, reservation2, reservation3);

        when(reservationRepository.findAll()).thenReturn(allReservations);

        // When
        List<Reservation> result = reservationService.getReservationsByRestaurant(restaurantId);

        // Then
        assertEquals(2, result.size());
        assertEquals("customer1@example.com", result.get(0).getCustomerEmail());
        assertEquals("customer3@example.com", result.get(1).getCustomerEmail());
        verify(reservationRepository).findAll();
    }

    @Test
    void getReservationsBySpace_ShouldReturnFilteredReservations() {
        // Given
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();
        UUID otherSpaceId = UUID.randomUUID();

        Reservation reservation1 = createTestReservation("customer1@example.com", 4);
        reservation1.setRestaurantId(restaurantId);
        reservation1.setSpaceId(spaceId);

        Reservation reservation2 = createTestReservation("customer2@example.com", 6);
        reservation2.setRestaurantId(restaurantId);
        reservation2.setSpaceId(otherSpaceId);

        Reservation reservation3 = createTestReservation("customer3@example.com", 2);
        reservation3.setRestaurantId(restaurantId);
        reservation3.setSpaceId(spaceId);

        List<Reservation> allReservations = Arrays.asList(reservation1, reservation2, reservation3);

        when(reservationRepository.findAll()).thenReturn(allReservations);

        // When
        List<Reservation> result = reservationService.getReservationsBySpace(restaurantId, spaceId);

        // Then
        assertEquals(2, result.size());
        assertEquals("customer1@example.com", result.get(0).getCustomerEmail());
        assertEquals("customer3@example.com", result.get(1).getCustomerEmail());
        verify(reservationRepository).findAll();
    }

    @Test
    void createReservation_WhenMultiDayReservation_ShouldThrowException() {
        // Given
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();

        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 18, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 21, 10, 0); // Next day

        Reservation reservation = createTestReservation("customer@example.com", 4);
        reservation.setRestaurantId(restaurantId);
        reservation.setSpaceId(spaceId);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);

        com.opentable.privatedining.model.Restaurant restaurant = new com.opentable.privatedining.model.Restaurant("Test Restaurant", "Address", "Cuisine", 50);
        Space space = new Space("Test Space", 2, 8);
        space.setId(spaceId);
        restaurant.setSpaces(List.of(space));

        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));

        // When & Then
        assertThrows(MultiDayReservationException.class, () -> {
            reservationService.createReservation(reservation);
        });
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void createReservation_WhenStartTimeBeforeOperatingHours_ShouldThrowException() {
        // Given
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();

        // 8:00 AM - before default operating hours (9:00 AM)
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 8, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 10, 0);

        Reservation reservation = createTestReservation("customer@example.com", 4);
        reservation.setRestaurantId(restaurantId);
        reservation.setSpaceId(spaceId);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);

        com.opentable.privatedining.model.Restaurant restaurant = new com.opentable.privatedining.model.Restaurant("Test Restaurant", "Address", "Cuisine", 50);
        Space space = new Space("Test Space", 2, 8);
        space.setId(spaceId);
        restaurant.setSpaces(List.of(space));

        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));

        // When & Then
        assertThrows(OutsideOperatingHoursException.class, () -> {
            reservationService.createReservation(reservation);
        });
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void createReservation_WhenEndTimeAfterOperatingHours_ShouldThrowException() {
        // Given
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();

        // Ends at 11:00 PM - after default operating hours (10:00 PM)
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 20, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 23, 0);

        Reservation reservation = createTestReservation("customer@example.com", 4);
        reservation.setRestaurantId(restaurantId);
        reservation.setSpaceId(spaceId);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);

        com.opentable.privatedining.model.Restaurant restaurant = new com.opentable.privatedining.model.Restaurant("Test Restaurant", "Address", "Cuisine", 50);
        Space space = new Space("Test Space", 2, 8);
        space.setId(spaceId);
        restaurant.setSpaces(List.of(space));

        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));

        // When & Then
        assertThrows(OutsideOperatingHoursException.class, () -> {
            reservationService.createReservation(reservation);
        });
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void createReservation_WhenWithinOperatingHours_ShouldSucceed() {
        // Given
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();

        // Within operating hours (9:00 AM - 10:00 PM)
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 12, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 14, 0);

        Reservation reservation = createTestReservation("customer@example.com", 4);
        reservation.setRestaurantId(restaurantId);
        reservation.setSpaceId(spaceId);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);

        com.opentable.privatedining.model.Restaurant restaurant = new com.opentable.privatedining.model.Restaurant("Test Restaurant", "Address", "Cuisine", 50);
        Space space = new Space("Test Space", 2, 8);
        space.setId(spaceId);
        restaurant.setSpaces(List.of(space));

        Reservation savedReservation = createTestReservation("customer@example.com", 4);
        savedReservation.setId(new ObjectId());

        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reservationRepository.findAll()).thenReturn(Arrays.asList());
        when(reservationRepository.save(reservation)).thenReturn(savedReservation);

        // When
        Reservation result = reservationService.createReservation(reservation);

        // Then
        assertNotNull(result);
        verify(reservationRepository).save(reservation);
    }

    @Test
    void createReservation_WhenWithinCustomOperatingHours_ShouldSucceed() {
        // Given
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();

        // Space with custom operating hours: 6:00 AM - 11:00 PM
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 6, 30);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 8, 30);

        Reservation reservation = createTestReservation("customer@example.com", 4);
        reservation.setRestaurantId(restaurantId);
        reservation.setSpaceId(spaceId);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);

        com.opentable.privatedining.model.Restaurant restaurant = new com.opentable.privatedining.model.Restaurant("Test Restaurant", "Address", "Cuisine", 50);
        Space space = new Space("Test Space", 2, 8);
        space.setId(spaceId);
        space.setOperatingStartTime(LocalTime.of(6, 0));
        space.setOperatingEndTime(LocalTime.of(23, 0));
        restaurant.setSpaces(List.of(space));

        Reservation savedReservation = createTestReservation("customer@example.com", 4);
        savedReservation.setId(new ObjectId());

        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reservationRepository.findAll()).thenReturn(Arrays.asList());
        when(reservationRepository.save(reservation)).thenReturn(savedReservation);

        // When
        Reservation result = reservationService.createReservation(reservation);

        // Then
        assertNotNull(result);
        verify(reservationRepository).save(reservation);
    }

    @Test
    void createReservation_WhenOutsideCustomOperatingHours_ShouldThrowException() {
        // Given
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();

        // Space with custom operating hours: 10:00 AM - 6:00 PM
        // Reservation is at 7:00 PM - outside custom hours
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 19, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 21, 0);

        Reservation reservation = createTestReservation("customer@example.com", 4);
        reservation.setRestaurantId(restaurantId);
        reservation.setSpaceId(spaceId);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);

        com.opentable.privatedining.model.Restaurant restaurant = new com.opentable.privatedining.model.Restaurant("Test Restaurant", "Address", "Cuisine", 50);
        Space space = new Space("Test Space", 2, 8);
        space.setId(spaceId);
        space.setOperatingStartTime(LocalTime.of(10, 0));
        space.setOperatingEndTime(LocalTime.of(18, 0));
        restaurant.setSpaces(List.of(space));

        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));

        // When & Then
        assertThrows(OutsideOperatingHoursException.class, () -> {
            reservationService.createReservation(reservation);
        });
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    // ==================== SLOT OPTIMIZATION TESTS ====================

    @Test
    void createReservation_WhenStartTimeNotAligned_ShouldRoundToNearestSlot_RoundDown() {
        // Given: Request at 12:17 with 60-min slots should align to 12:00 (round down)
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();

        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 12, 17);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 14, 17);

        Reservation reservation = createTestReservation("customer@example.com", 4);
        reservation.setRestaurantId(restaurantId);
        reservation.setSpaceId(spaceId);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);

        com.opentable.privatedining.model.Restaurant restaurant = new com.opentable.privatedining.model.Restaurant("Test Restaurant", "Address", "Cuisine", 50);
        Space space = new Space("Test Space", 2, 8);
        space.setId(spaceId);
        space.setTimeSlotDurationMinutes(60);
        restaurant.setSpaces(List.of(space));

        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reservationRepository.findAll()).thenReturn(Arrays.asList());
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Reservation result = reservationService.createReservation(reservation);

        // Then
        assertNotNull(result);
        assertEquals(LocalDateTime.of(2026, 1, 20, 12, 0), result.getStartTime());
        assertEquals(LocalDateTime.of(2026, 1, 20, 15, 0), result.getEndTime()); // End rounds up
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void createReservation_WhenStartTimeNotAligned_ShouldRoundToNearestSlot_RoundUp() {
        // Given: Request at 12:45 with 60-min slots should align to 13:00 (round up)
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();

        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 12, 45);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 14, 45);

        Reservation reservation = createTestReservation("customer@example.com", 4);
        reservation.setRestaurantId(restaurantId);
        reservation.setSpaceId(spaceId);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);

        com.opentable.privatedining.model.Restaurant restaurant = new com.opentable.privatedining.model.Restaurant("Test Restaurant", "Address", "Cuisine", 50);
        Space space = new Space("Test Space", 2, 8);
        space.setId(spaceId);
        space.setTimeSlotDurationMinutes(60);
        restaurant.setSpaces(List.of(space));

        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reservationRepository.findAll()).thenReturn(Arrays.asList());
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Reservation result = reservationService.createReservation(reservation);

        // Then
        assertNotNull(result);
        assertEquals(LocalDateTime.of(2026, 1, 20, 13, 0), result.getStartTime());
        assertEquals(LocalDateTime.of(2026, 1, 20, 15, 0), result.getEndTime()); // End rounds up
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void createReservation_WhenTimesAlreadyAligned_ShouldRemainUnchanged() {
        // Given: Request already on slot boundaries
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();

        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 12, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 14, 0);

        Reservation reservation = createTestReservation("customer@example.com", 4);
        reservation.setRestaurantId(restaurantId);
        reservation.setSpaceId(spaceId);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);

        com.opentable.privatedining.model.Restaurant restaurant = new com.opentable.privatedining.model.Restaurant("Test Restaurant", "Address", "Cuisine", 50);
        Space space = new Space("Test Space", 2, 8);
        space.setId(spaceId);
        space.setTimeSlotDurationMinutes(60);
        restaurant.setSpaces(List.of(space));

        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reservationRepository.findAll()).thenReturn(Arrays.asList());
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Reservation result = reservationService.createReservation(reservation);

        // Then
        assertNotNull(result);
        assertEquals(LocalDateTime.of(2026, 1, 20, 12, 0), result.getStartTime());
        assertEquals(LocalDateTime.of(2026, 1, 20, 14, 0), result.getEndTime());
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void createReservation_WhenDurationLessThanOneSlot_ShouldThrowException() {
        // Given: After alignment, duration is less than one slot
        // Start 12:45 rounds to 13:00, end 13:10 rounds to 14:00 => 60 min = OK
        // Start 12:45 rounds to 13:00, end 13:00 stays at 13:00 => 0 min = FAIL
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();

        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 12, 45); // Rounds to 13:00
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 13, 0);    // Already aligned, stays 13:00

        Reservation reservation = createTestReservation("customer@example.com", 4);
        reservation.setRestaurantId(restaurantId);
        reservation.setSpaceId(spaceId);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);

        com.opentable.privatedining.model.Restaurant restaurant = new com.opentable.privatedining.model.Restaurant("Test Restaurant", "Address", "Cuisine", 50);
        Space space = new Space("Test Space", 2, 8);
        space.setId(spaceId);
        space.setTimeSlotDurationMinutes(60);
        restaurant.setSpaces(List.of(space));

        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));

        // When & Then
        assertThrows(InvalidReservationDurationException.class, () -> {
            reservationService.createReservation(reservation);
        });
        verify(reservationRepository, never()).save(any(Reservation.class));
    }

    @Test
    void createReservation_WithCustom30MinSlots_ShouldAlignCorrectly() {
        // Given: Request at 12:17 with 30-min slots should align to 12:15 (nearest is 12:00 or 12:30, 17 is closer to 30)
        // Actually 17 min: remainder = 17, slotMinutes/2 = 15, 17 >= 15, so round up to 12:30
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();

        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 12, 17);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 13, 17);

        Reservation reservation = createTestReservation("customer@example.com", 4);
        reservation.setRestaurantId(restaurantId);
        reservation.setSpaceId(spaceId);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);

        com.opentable.privatedining.model.Restaurant restaurant = new com.opentable.privatedining.model.Restaurant("Test Restaurant", "Address", "Cuisine", 50);
        Space space = new Space("Test Space", 2, 8);
        space.setId(spaceId);
        space.setTimeSlotDurationMinutes(30);
        restaurant.setSpaces(List.of(space));

        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reservationRepository.findAll()).thenReturn(Arrays.asList());
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Reservation result = reservationService.createReservation(reservation);

        // Then
        assertNotNull(result);
        assertEquals(LocalDateTime.of(2026, 1, 20, 12, 30), result.getStartTime());
        assertEquals(LocalDateTime.of(2026, 1, 20, 13, 30), result.getEndTime());
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void createReservation_WhenEndTimeNotAligned_ShouldRoundUp() {
        // Given: End time at 14:10 should round up to 15:00 with 60-min slots
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();

        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 12, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 14, 10);

        Reservation reservation = createTestReservation("customer@example.com", 4);
        reservation.setRestaurantId(restaurantId);
        reservation.setSpaceId(spaceId);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);

        com.opentable.privatedining.model.Restaurant restaurant = new com.opentable.privatedining.model.Restaurant("Test Restaurant", "Address", "Cuisine", 50);
        Space space = new Space("Test Space", 2, 8);
        space.setId(spaceId);
        space.setTimeSlotDurationMinutes(60);
        restaurant.setSpaces(List.of(space));

        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reservationRepository.findAll()).thenReturn(Arrays.asList());
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Reservation result = reservationService.createReservation(reservation);

        // Then
        assertNotNull(result);
        assertEquals(LocalDateTime.of(2026, 1, 20, 12, 0), result.getStartTime());
        assertEquals(LocalDateTime.of(2026, 1, 20, 15, 0), result.getEndTime());
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void createReservation_WithDefaultSlotDuration_ShouldUse60Minutes() {
        // Given: Space without explicit slot duration should use default 60 minutes
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();

        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 12, 20);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 14, 20);

        Reservation reservation = createTestReservation("customer@example.com", 4);
        reservation.setRestaurantId(restaurantId);
        reservation.setSpaceId(spaceId);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);

        com.opentable.privatedining.model.Restaurant restaurant = new com.opentable.privatedining.model.Restaurant("Test Restaurant", "Address", "Cuisine", 50);
        Space space = new Space("Test Space", 2, 8);
        space.setId(spaceId);
        // Not setting timeSlotDurationMinutes - should default to 60
        restaurant.setSpaces(List.of(space));

        when(restaurantService.getRestaurantById(restaurantId)).thenReturn(Optional.of(restaurant));
        when(reservationRepository.findAll()).thenReturn(Arrays.asList());
        when(reservationRepository.save(any(Reservation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Reservation result = reservationService.createReservation(reservation);

        // Then
        assertNotNull(result);
        assertEquals(LocalDateTime.of(2026, 1, 20, 12, 0), result.getStartTime()); // 20 < 30, round down
        assertEquals(LocalDateTime.of(2026, 1, 20, 15, 0), result.getEndTime()); // Round up
        verify(reservationRepository).save(any(Reservation.class));
    }

    private Reservation createTestReservation(String customerEmail, int partySize) {
        Reservation reservation = new Reservation();
        reservation.setCustomerEmail(customerEmail);
        reservation.setPartySize(partySize);
        reservation.setRestaurantId(new ObjectId());
        reservation.setSpaceId(UUID.randomUUID());
        reservation.setStartTime(LocalDateTime.now().plusDays(1));
        reservation.setEndTime(LocalDateTime.now().plusDays(1).plusHours(2));
        reservation.setStatus("CONFIRMED");
        return reservation;
    }
}