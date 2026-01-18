package com.opentable.privatedining.service;

import com.opentable.privatedining.exception.CapacityExceededException;
import com.opentable.privatedining.model.Reservation;
import com.opentable.privatedining.model.Space;
import com.opentable.privatedining.repository.ReservationRepository;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service responsible for validating space capacity for reservations.
 * Allows concurrent reservations as long as the total headcount remains within the space's maximum capacity.
 */
@Service
public class CapacityValidationService {

    private final ReservationRepository reservationRepository;

    public CapacityValidationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    /**
     * Validates that a new reservation can be accommodated within the space's capacity limits.
     * Throws CapacityExceededException if the combined headcount would exceed maxCapacity.
     *
     * @param reservation the new reservation to validate
     * @param space the space being reserved
     * @throws CapacityExceededException if combined headcount would exceed max capacity
     */
    public void validateCapacity(Reservation reservation, Space space) {
        int currentOccupancy = calculateCurrentOccupancy(
            reservation.getRestaurantId(),
            reservation.getSpaceId(),
            reservation.getStartTime(),
            reservation.getEndTime()
        );

        int newTotalOccupancy = currentOccupancy + reservation.getPartySize();

        if (newTotalOccupancy > space.getMaxCapacity()) {
            throw new CapacityExceededException(
                reservation.getRestaurantId(),
                reservation.getSpaceId(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getPartySize(),
                currentOccupancy,
                space.getMaxCapacity()
            );
        }
    }

    /**
     * Validates capacity excluding a specific reservation (for updates).
     *
     * @param reservation the reservation to validate
     * @param space the space being reserved
     * @param excludeReservationId the reservation ID to exclude from occupancy calculation
     * @throws CapacityExceededException if combined headcount would exceed max capacity
     */
    public void validateCapacityExcluding(Reservation reservation, Space space, ObjectId excludeReservationId) {
        int currentOccupancy = calculateCurrentOccupancyExcluding(
            reservation.getRestaurantId(),
            reservation.getSpaceId(),
            reservation.getStartTime(),
            reservation.getEndTime(),
            excludeReservationId
        );

        int newTotalOccupancy = currentOccupancy + reservation.getPartySize();

        if (newTotalOccupancy > space.getMaxCapacity()) {
            throw new CapacityExceededException(
                reservation.getRestaurantId(),
                reservation.getSpaceId(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getPartySize(),
                currentOccupancy,
                space.getMaxCapacity()
            );
        }
    }

    /**
     * Calculates the current total occupancy (sum of party sizes) for overlapping reservations.
     *
     * @param restaurantId the restaurant ID
     * @param spaceId the space ID
     * @param startTime the start time of the new reservation
     * @param endTime the end time of the new reservation
     * @return the sum of party sizes for all overlapping reservations
     */
    public int calculateCurrentOccupancy(ObjectId restaurantId, UUID spaceId,
                                         LocalDateTime startTime, LocalDateTime endTime) {
        List<Reservation> overlappingReservations = reservationRepository.findOverlappingReservations(
            restaurantId, spaceId, startTime, endTime);

        return overlappingReservations.stream()
            .mapToInt(Reservation::getPartySize)
            .sum();
    }

    /**
     * Calculates the current occupancy excluding a specific reservation.
     *
     * @param restaurantId the restaurant ID
     * @param spaceId the space ID
     * @param startTime the start time
     * @param endTime the end time
     * @param excludeReservationId the reservation ID to exclude
     * @return the sum of party sizes excluding the specified reservation
     */
    public int calculateCurrentOccupancyExcluding(ObjectId restaurantId, UUID spaceId,
                                                   LocalDateTime startTime, LocalDateTime endTime,
                                                   ObjectId excludeReservationId) {
        List<Reservation> overlappingReservations = reservationRepository.findOverlappingReservations(
            restaurantId, spaceId, startTime, endTime);

        return overlappingReservations.stream()
            .filter(r -> !r.getId().equals(excludeReservationId))
            .mapToInt(Reservation::getPartySize)
            .sum();
    }

    /**
     * Returns the available capacity for a given time slot.
     *
     * @param restaurantId the restaurant ID
     * @param spaceId the space ID
     * @param startTime the start time
     * @param endTime the end time
     * @param maxCapacity the maximum capacity of the space
     * @return the remaining available capacity
     */
    public int getAvailableCapacity(ObjectId restaurantId, UUID spaceId,
                                    LocalDateTime startTime, LocalDateTime endTime,
                                    int maxCapacity) {
        int currentOccupancy = calculateCurrentOccupancy(restaurantId, spaceId, startTime, endTime);
        return Math.max(0, maxCapacity - currentOccupancy);
    }
}

