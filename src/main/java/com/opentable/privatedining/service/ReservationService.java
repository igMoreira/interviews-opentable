package com.opentable.privatedining.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bson.types.ObjectId;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import com.opentable.privatedining.exception.InvalidPartySizeException;
import com.opentable.privatedining.exception.InvalidReservationDurationException;
import com.opentable.privatedining.exception.MultiDayReservationException;
import com.opentable.privatedining.exception.OutsideOperatingHoursException;
import com.opentable.privatedining.exception.RestaurantNotFoundException;
import com.opentable.privatedining.exception.SpaceNotFoundException;
import com.opentable.privatedining.model.Reservation;
import com.opentable.privatedining.model.Restaurant;
import com.opentable.privatedining.model.Space;
import com.opentable.privatedining.repository.ReservationRepository;

/**
 * Service class for managing reservation operations.
 */
@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final RestaurantService restaurantService;
    private final CapacityValidationService capacityValidationService;

    /**
     * Constructs a new ReservationService with the required dependencies.
     *
     * @param reservationRepository the repository for reservation data access
     * @param restaurantService the service for restaurant operations
     * @param capacityValidationService the service for capacity validation
     */
    public ReservationService(ReservationRepository reservationRepository,
                              RestaurantService restaurantService,
                              CapacityValidationService capacityValidationService) {
        this.reservationRepository = reservationRepository;
        this.restaurantService = restaurantService;
        this.capacityValidationService = capacityValidationService;
    }

    /**
     * Retrieves all reservations.
     *
     * @return list of all reservations
     */
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    /**
     * Retrieves a reservation by its ID.
     *
     * @param id the reservation ID
     * @return optional containing the reservation if found
     */
    public Optional<Reservation> getReservationById(ObjectId id) {
        return reservationRepository.findById(id);
    }

    /**
     * Creates a new reservation with validation.
     * Validates restaurant and space existence, operating hours, party size, and capacity.
     * Automatically aligns reservation times to time slot boundaries.
     *
     * @param reservation the reservation to create
     * @return the created reservation
     * @throws RestaurantNotFoundException if the restaurant doesn't exist
     * @throws SpaceNotFoundException if the space doesn't exist
     * @throws MultiDayReservationException if reservation spans multiple days
     * @throws InvalidReservationDurationException if duration is less than minimum slot duration
     * @throws OutsideOperatingHoursException if reservation is outside operating hours
     * @throws InvalidPartySizeException if party size exceeds space capacity
     * @throws com.opentable.privatedining.exception.CapacityExceededException if combined capacity would be exceeded
     */
    @CacheEvict(value = "occupancyReports", allEntries = true)
    public Reservation createReservation(Reservation reservation) {
        // Validate that the restaurant exists
        Optional<com.opentable.privatedining.model.Restaurant> restaurantOpt =
            restaurantService.getRestaurantById(reservation.getRestaurantId());
        if (restaurantOpt.isEmpty()) {
            throw new RestaurantNotFoundException(reservation.getRestaurantId());
        }

        //validate that the space exists within the restaurant
        Restaurant restaurant = restaurantOpt.get();
        Space space = restaurant.getSpaces().stream().filter(s -> s.getId().equals(reservation.getSpaceId()))
            .findFirst()
            .orElseThrow(() -> new SpaceNotFoundException(reservation.getRestaurantId(), reservation.getSpaceId()));

        // Validate that reservation does not span multiple days
        if (!reservation.getStartTime().toLocalDate().equals(reservation.getEndTime().toLocalDate())) {
            throw new MultiDayReservationException(
                reservation.getStartTime().toLocalDate(),
                reservation.getEndTime().toLocalDate());
        }

        // Align reservation times to time slots
        int slotDuration = space.getTimeSlotDurationMinutes();
        LocalDateTime alignedStartTime = alignStartTimeToNearestSlot(reservation.getStartTime(), slotDuration);
        LocalDateTime alignedEndTime = alignEndTimeToSlotCeiling(reservation.getEndTime(), slotDuration);

        // Ensure minimum reservation duration of one slot
        long durationMinutes = Duration.between(alignedStartTime, alignedEndTime).toMinutes();
        if (durationMinutes < slotDuration) {
            throw new InvalidReservationDurationException(slotDuration);
        }

        // Update reservation with aligned times
        reservation.setStartTime(alignedStartTime);
        reservation.setEndTime(alignedEndTime);

        // Validate reservation is within operating hours (after alignment)
        LocalTime reservationStartTime = reservation.getStartTime().toLocalTime();
        LocalTime reservationEndTime = reservation.getEndTime().toLocalTime();
        LocalTime operatingStart = space.getOperatingStartTime();
        LocalTime operatingEnd = space.getOperatingEndTime();

        if (reservationStartTime.isBefore(operatingStart) || reservationEndTime.isAfter(operatingEnd)) {
            throw new OutsideOperatingHoursException(
                reservationStartTime, reservationEndTime, operatingStart, operatingEnd);
        }

        // Validate party size is within space capacity (per-reservation validation)
        if (reservation.getPartySize() < space.getMinCapacity() ||
            reservation.getPartySize() > space.getMaxCapacity()) {
            throw new InvalidPartySizeException(
                reservation.getPartySize(), space.getMinCapacity(), space.getMaxCapacity());
        }

        // Validate capacity allows concurrent reservations (combined headcount must not exceed maxCapacity)
        capacityValidationService.validateCapacity(reservation, space);

        return reservationRepository.save(reservation);
    }

    /**
     * Deletes a reservation by its ID.
     *
     * @param id the reservation ID
     * @return true if the reservation was deleted, false if not found
     */
    @CacheEvict(value = "occupancyReports", allEntries = true)
    public boolean deleteReservation(ObjectId id) {
        Optional<Reservation> existingReservation = reservationRepository.findById(id);
        if (existingReservation.isPresent()) {
            reservationRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Retrieves all reservations for a specific restaurant.
     *
     * @param restaurantId the restaurant ID
     * @return list of reservations for the restaurant
     */
    public List<Reservation> getReservationsByRestaurant(ObjectId restaurantId) {
        return reservationRepository.findAll().stream()
            .filter(reservation -> reservation.getRestaurantId().equals(restaurantId))
                .toList();
    }

    /**
     * Retrieves all reservations for a specific space within a restaurant.
     *
     * @param restaurantId the restaurant ID
     * @param spaceId the space UUID
     * @return list of reservations for the space
     */
    public List<Reservation> getReservationsBySpace(ObjectId restaurantId, UUID spaceId) {
        return reservationRepository.findAll().stream()
            .filter(reservation -> reservation.getRestaurantId().equals(restaurantId) &&
                reservation.getSpaceId().equals(spaceId))
            .toList();
    }


    /**
     * Aligns start time to the nearest slot boundary.
     * Example with 60-min slots: 12:17 -> 12:00, 12:45 -> 13:00
     */
    private LocalDateTime alignStartTimeToNearestSlot(LocalDateTime time, int slotMinutes) {
        int minute = time.getMinute();
        int remainder = minute % slotMinutes;

        LocalDateTime baseTime = time.withMinute(0).withSecond(0).withNano(0);
        int slotStart = (minute / slotMinutes) * slotMinutes;

        // Round to nearest: if past halfway point of slot, round up
        if (remainder >= slotMinutes / 2.0) {
            return baseTime.plusMinutes(slotStart + slotMinutes);
        } else {
            return baseTime.plusMinutes(slotStart);
        }
    }

    /**
     * Aligns end time by rounding up to the next slot boundary (ceiling).
     * Example with 60-min slots: 14:10 -> 15:00, 14:00 -> 14:00 (already aligned)
     */
    private LocalDateTime alignEndTimeToSlotCeiling(LocalDateTime time, int slotMinutes) {
        int minute = time.getMinute();
        int remainder = minute % slotMinutes;

        if (remainder == 0 && time.getSecond() == 0 && time.getNano() == 0) {
            // Already aligned
            return time;
        }

        LocalDateTime baseTime = time.withMinute(0).withSecond(0).withNano(0);
        int slotStart = (minute / slotMinutes) * slotMinutes;

        // Round up to next slot boundary
        return baseTime.plusMinutes(slotStart + slotMinutes);
    }
}