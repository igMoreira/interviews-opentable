package com.opentable.privatedining.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Occupancy report for a specific space")
public class SpaceOccupancyReport {

    @Schema(description = "Unique identifier of the space", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID spaceId;

    @Schema(description = "Name of the space", example = "Garden Room")
    private String spaceName;

    @Schema(description = "Maximum capacity of the space", example = "25")
    private int maxCapacity;

    @Schema(description = "Total number of reservations in the report period", example = "7")
    private int totalReservations;

    @Schema(description = "Peak occupancy (highest guest count) during the period", example = "18")
    private int peakOccupancy;

    @Schema(description = "Average utilization percentage across all time slots", example = "52.3")
    private double averageUtilization;

    @Schema(description = "Hourly breakdown of occupancy data")
    private List<TimeSlotOccupancy> hourlyBreakdown;
}

