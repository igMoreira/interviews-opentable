package com.opentable.privatedining.service;

import com.opentable.privatedining.config.AnalyticsConfig;
import com.opentable.privatedining.dto.*;
import com.opentable.privatedining.exception.InvalidDateRangeException;
import com.opentable.privatedining.exception.RestaurantNotFoundException;
import com.opentable.privatedining.exception.SpaceNotFoundException;
import com.opentable.privatedining.model.Reservation;
import com.opentable.privatedining.model.Restaurant;
import com.opentable.privatedining.model.Space;
import com.opentable.privatedining.repository.ReservationRepository;
import com.opentable.privatedining.repository.RestaurantRepository;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service for generating occupancy analytics reports.
 * Provides detailed breakdown of occupancy levels for restaurant spaces over a specified time range.
 */
@Service
public class OccupancyAnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(OccupancyAnalyticsService.class);

    private final ReservationRepository reservationRepository;
    private final RestaurantRepository restaurantRepository;
    private final AnalyticsConfig analyticsConfig;

    /**
     * Constructs a new OccupancyAnalyticsService with the required dependencies.
     *
     * @param reservationRepository the repository for reservation data access
     * @param restaurantRepository the repository for restaurant data access
     * @param analyticsConfig the configuration for analytics settings
     */
    public OccupancyAnalyticsService(ReservationRepository reservationRepository,
                                      RestaurantRepository restaurantRepository,
                                      AnalyticsConfig analyticsConfig) {
        this.reservationRepository = reservationRepository;
        this.restaurantRepository = restaurantRepository;
        this.analyticsConfig = analyticsConfig;
    }

    /**
     * Generates an occupancy report for a restaurant within the specified time range.
     *
     * @param restaurantId the restaurant ID
     * @param startTime the start of the report period
     * @param endTime the end of the report period
     * @param spaceId optional space ID to filter the report to a single space
     * @param page the page number (0-based)
     * @param size the page size
     * @return the occupancy report response
     * @throws InvalidDateRangeException if the date range is invalid
     * @throws RestaurantNotFoundException if the restaurant is not found
     * @throws SpaceNotFoundException if the specified space is not found
     */
    public OccupancyReportResponse generateOccupancyReport(
            ObjectId restaurantId,
            LocalDateTime startTime,
            LocalDateTime endTime,
            UUID spaceId,
            int page,
            int size) {

        validateDateRange(startTime, endTime);

        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RestaurantNotFoundException(restaurantId));

        List<Space> spacesToReport = getSpacesToReport(restaurant, spaceId);

        List<Reservation> reservations = fetchReservations(restaurantId, startTime, endTime, spaceId);

        logger.debug("Generating occupancy report for restaurant {} with {} reservations across {} spaces",
                restaurantId, reservations.size(), spacesToReport.size());

        // Generate reports for all spaces (needed for summary)
        List<SpaceOccupancyReport> allSpaceReports = new ArrayList<>();
        for (Space space : spacesToReport) {
            SpaceOccupancyReport spaceReport = generateSpaceReport(space, reservations, startTime, endTime);
            allSpaceReports.add(spaceReport);
        }

        // Calculate summary from all spaces
        OccupancySummary summary = calculateSummary(allSpaceReports, reservations, spacesToReport);

        // Apply pagination to space reports
        int totalElements = allSpaceReports.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int fromIndex = Math.min(page * size, totalElements);
        int toIndex = Math.min(fromIndex + size, totalElements);
        List<SpaceOccupancyReport> paginatedReports = allSpaceReports.subList(fromIndex, toIndex);

        return new OccupancyReportResponse(
                restaurantId.toHexString(),
                startTime,
                endTime,
                summary,
                paginatedReports,
                page,
                size,
                totalElements,
                totalPages
        );
    }

    /**
     * Validates that the date range is valid and within allowed limits.
     */
    private void validateDateRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new InvalidDateRangeException("Start time and end time are required");
        }

        if (!endTime.isAfter(startTime)) {
            throw new InvalidDateRangeException(startTime, endTime, "End time must be after start time");
        }

        long daysBetween = ChronoUnit.DAYS.between(startTime, endTime);
        if (daysBetween > analyticsConfig.getMaxRangeDays()) {
            throw new InvalidDateRangeException(startTime, endTime, analyticsConfig.getMaxRangeDays());
        }
    }

    /**
     * Gets the list of spaces to include in the report.
     */
    private List<Space> getSpacesToReport(Restaurant restaurant, UUID spaceId) {
        if (spaceId == null) {
            return restaurant.getSpaces();
        }

        return restaurant.getSpaces().stream()
                .filter(space -> space.getId().equals(spaceId))
                .findFirst()
                .map(List::of)
                .orElseThrow(() -> new SpaceNotFoundException(spaceId));
    }

    /**
     * Fetches reservations based on whether a specific space is requested.
     */
    private List<Reservation> fetchReservations(ObjectId restaurantId, LocalDateTime startTime,
                                                 LocalDateTime endTime, UUID spaceId) {
        if (spaceId != null) {
            return reservationRepository.findByRestaurantIdAndSpaceIdAndTimeRange(
                    restaurantId, spaceId, startTime, endTime);
        }
        return reservationRepository.findByRestaurantIdAndTimeRange(restaurantId, startTime, endTime);
    }

    /**
     * Generates an occupancy report for a single space.
     */
    private SpaceOccupancyReport generateSpaceReport(Space space, List<Reservation> allReservations,
                                                      LocalDateTime startTime, LocalDateTime endTime) {
        List<Reservation> spaceReservations = allReservations.stream()
                .filter(r -> r.getSpaceId().equals(space.getId()))
                .toList();

        List<TimeSlotOccupancy> hourlyBreakdown = generateHourlyBreakdown(
                space, spaceReservations, startTime, endTime);

        int peakOccupancy = hourlyBreakdown.stream()
                .mapToInt(TimeSlotOccupancy::getOccupancy)
                .max()
                .orElse(0);

        double averageUtilization = hourlyBreakdown.stream()
                .mapToDouble(TimeSlotOccupancy::getUtilizationPercentage)
                .average()
                .orElse(0.0);

        return new SpaceOccupancyReport(
                space.getId(),
                space.getName(),
                space.getMaxCapacity(),
                spaceReservations.size(),
                peakOccupancy,
                roundToTwoDecimals(averageUtilization),
                hourlyBreakdown
        );
    }

    /**
     * Generates hourly breakdown of occupancy for a space.
     */
    private List<TimeSlotOccupancy> generateHourlyBreakdown(Space space, List<Reservation> reservations,
                                                            LocalDateTime startTime, LocalDateTime endTime) {
        List<TimeSlotOccupancy> slots = new ArrayList<>();
        int slotDurationMinutes = analyticsConfig.getTimeSlotDurationMinutes();

        LocalDateTime current = startTime.truncatedTo(ChronoUnit.HOURS);

        while (current.isBefore(endTime)) {
            LocalDateTime slotEnd = current.plusMinutes(slotDurationMinutes);

            final LocalDateTime slotStart = current;

            // Count reservations and occupancy for this slot
            List<Reservation> overlappingReservations = reservations.stream()
                    .filter(r -> overlaps(r, slotStart, slotEnd))
                    .toList();

            int reservationCount = overlappingReservations.size();
            int occupancy = overlappingReservations.stream()
                    .mapToInt(Reservation::getPartySize)
                    .sum();

            double utilization = space.getMaxCapacity() > 0
                    ? (double) occupancy / space.getMaxCapacity() * 100
                    : 0.0;

            slots.add(new TimeSlotOccupancy(
                    slotStart,
                    slotEnd,
                    reservationCount,
                    occupancy,
                    space.getMaxCapacity(),
                    roundToTwoDecimals(utilization)
            ));

            current = slotEnd;
        }

        return slots;
    }

    /**
     * Checks if a reservation overlaps with a given time slot.
     */
    private boolean overlaps(Reservation reservation, LocalDateTime slotStart, LocalDateTime slotEnd) {
        return reservation.getStartTime().isBefore(slotEnd) && reservation.getEndTime().isAfter(slotStart);
    }

    /**
     * Calculates the summary metrics from all space reports.
     */
    private OccupancySummary calculateSummary(List<SpaceOccupancyReport> spaceReports,
                                               List<Reservation> reservations,
                                               List<Space> spaces) {
        int totalReservations = reservations.size();

        int totalGuests = reservations.stream()
                .mapToInt(Reservation::getPartySize)
                .sum();

        int peakOccupancy = spaceReports.stream()
                .mapToInt(SpaceOccupancyReport::getPeakOccupancy)
                .max()
                .orElse(0);

        double averageUtilization = spaceReports.stream()
                .mapToDouble(SpaceOccupancyReport::getAverageUtilization)
                .average()
                .orElse(0.0);

        int totalMaxCapacity = spaces.stream()
                .mapToInt(Space::getMaxCapacity)
                .sum();

        double overallUtilization = totalMaxCapacity > 0
                ? (double) peakOccupancy / totalMaxCapacity * 100
                : 0.0;

        return new OccupancySummary(
                totalReservations,
                totalGuests,
                peakOccupancy,
                roundToTwoDecimals(averageUtilization),
                roundToTwoDecimals(overallUtilization)
        );
    }

    /**
     * Rounds a double value to two decimal places.
     */
    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}

