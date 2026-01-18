package com.opentable.privatedining.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Occupancy data for a specific time slot")
public class TimeSlotOccupancy {

    @Schema(description = "Start time of the slot", example = "2026-01-20T12:00:00")
    private LocalDateTime slotStart;

    @Schema(description = "End time of the slot", example = "2026-01-20T13:00:00")
    private LocalDateTime slotEnd;

    @Schema(description = "Number of reservations in this slot", example = "3")
    private int reservationCount;

    @Schema(description = "Total number of guests (party sizes) in this slot", example = "18")
    private int occupancy;

    @Schema(description = "Maximum capacity of the space", example = "25")
    private int maxCapacity;

    @Schema(description = "Utilization percentage (occupancy / maxCapacity * 100)", example = "72.0")
    private double utilizationPercentage;

    public TimeSlotOccupancy() {}

    public TimeSlotOccupancy(LocalDateTime slotStart, LocalDateTime slotEnd, int reservationCount,
                             int occupancy, int maxCapacity, double utilizationPercentage) {
        this.slotStart = slotStart;
        this.slotEnd = slotEnd;
        this.reservationCount = reservationCount;
        this.occupancy = occupancy;
        this.maxCapacity = maxCapacity;
        this.utilizationPercentage = utilizationPercentage;
    }

    public LocalDateTime getSlotStart() {
        return slotStart;
    }

    public void setSlotStart(LocalDateTime slotStart) {
        this.slotStart = slotStart;
    }

    public LocalDateTime getSlotEnd() {
        return slotEnd;
    }

    public void setSlotEnd(LocalDateTime slotEnd) {
        this.slotEnd = slotEnd;
    }

    public int getReservationCount() {
        return reservationCount;
    }

    public void setReservationCount(int reservationCount) {
        this.reservationCount = reservationCount;
    }

    public int getOccupancy() {
        return occupancy;
    }

    public void setOccupancy(int occupancy) {
        this.occupancy = occupancy;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public double getUtilizationPercentage() {
        return utilizationPercentage;
    }

    public void setUtilizationPercentage(double utilizationPercentage) {
        this.utilizationPercentage = utilizationPercentage;
    }
}

