package com.opentable.privatedining.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

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

    public SpaceOccupancyReport() {}

    public SpaceOccupancyReport(UUID spaceId, String spaceName, int maxCapacity, int totalReservations,
                                 int peakOccupancy, double averageUtilization, List<TimeSlotOccupancy> hourlyBreakdown) {
        this.spaceId = spaceId;
        this.spaceName = spaceName;
        this.maxCapacity = maxCapacity;
        this.totalReservations = totalReservations;
        this.peakOccupancy = peakOccupancy;
        this.averageUtilization = averageUtilization;
        this.hourlyBreakdown = hourlyBreakdown;
    }

    public UUID getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(UUID spaceId) {
        this.spaceId = spaceId;
    }

    public String getSpaceName() {
        return spaceName;
    }

    public void setSpaceName(String spaceName) {
        this.spaceName = spaceName;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public int getTotalReservations() {
        return totalReservations;
    }

    public void setTotalReservations(int totalReservations) {
        this.totalReservations = totalReservations;
    }

    public int getPeakOccupancy() {
        return peakOccupancy;
    }

    public void setPeakOccupancy(int peakOccupancy) {
        this.peakOccupancy = peakOccupancy;
    }

    public double getAverageUtilization() {
        return averageUtilization;
    }

    public void setAverageUtilization(double averageUtilization) {
        this.averageUtilization = averageUtilization;
    }

    public List<TimeSlotOccupancy> getHourlyBreakdown() {
        return hourlyBreakdown;
    }

    public void setHourlyBreakdown(List<TimeSlotOccupancy> hourlyBreakdown) {
        this.hourlyBreakdown = hourlyBreakdown;
    }
}

