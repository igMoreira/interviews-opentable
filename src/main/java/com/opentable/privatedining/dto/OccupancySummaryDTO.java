package com.opentable.privatedining.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Summary of occupancy metrics for the entire report period")
public class OccupancySummaryDTO {

    @Schema(description = "Total number of reservations across all spaces", example = "12")
    private int totalReservations;

    @Schema(description = "Total number of guests across all reservations", example = "58")
    private int totalGuests;

    @Schema(description = "Highest occupancy recorded in any single time slot", example = "18")
    private int peakOccupancy;

    @Schema(description = "Average utilization percentage across all spaces and time slots", example = "45.5")
    private double averageUtilization;

    @Schema(description = "Overall utilization percentage based on peak occupancy vs total capacity", example = "72.0")
    private double overallUtilizationPercentage;
}

