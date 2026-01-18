package com.opentable.privatedining.repository;

import com.opentable.privatedining.model.Reservation;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Repository interface for Reservation entities.
 */
@Repository
public interface ReservationRepository extends MongoRepository<Reservation, ObjectId> {

    /**
     * Find all reservations that overlap with the given time range for a specific space.
     * Two time ranges overlap if: newStart < existingEnd AND newEnd > existingStart
     */
    @Query("{ 'restaurantId': ?0, 'spaceId': ?1, 'startTime': { $lt: ?3 }, 'endTime': { $gt: ?2 } }")
    List<Reservation> findOverlappingReservations(ObjectId restaurantId, UUID spaceId,
                                                   LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Find all reservations for a restaurant within a date/time range.
     * Used for occupancy analytics reporting.
     */
    @Query("{ 'restaurantId': ?0, 'startTime': { $lt: ?2 }, 'endTime': { $gt: ?1 } }")
    List<Reservation> findByRestaurantIdAndTimeRange(ObjectId restaurantId,
                                                      LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Find all reservations for a specific space within a date/time range.
     * Used for occupancy analytics reporting with space filtering.
     */
    @Query("{ 'restaurantId': ?0, 'spaceId': ?1, 'startTime': { $lt: ?3 }, 'endTime': { $gt: ?2 } }")
    List<Reservation> findByRestaurantIdAndSpaceIdAndTimeRange(ObjectId restaurantId, UUID spaceId,
                                                                LocalDateTime startTime, LocalDateTime endTime);
}