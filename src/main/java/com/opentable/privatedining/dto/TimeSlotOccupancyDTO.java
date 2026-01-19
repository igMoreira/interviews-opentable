package com.opentable.privatedining.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Occupancy data for a specific time slot")
public class TimeSlotOccupancyDTO {

    @Schema(description = "Start time of the slot", example = "2026-01-20 12:00")
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
    private LocalDateTime slotStart;

    @Schema(description = "End time of the slot", example = "2026-01-20 13:00")
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
    private LocalDateTime slotEnd;

    @Schema(description = "Number of reservations in this slot", example = "3")
    private int reservationCount;

    @Schema(description = "Total number of guests (party sizes) in this slot", example = "18")
    private int occupancy;

    @Schema(description = "Maximum capacity of the space", example = "25")
    private int maxCapacity;

    @Schema(description = "Utilization percentage (occupancy / maxCapacity * 100)", example = "72.0")
    private double utilizationPercentage;
}

