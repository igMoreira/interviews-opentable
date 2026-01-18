package com.opentable.privatedining.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Summary of occupancy metrics for the entire report period")
public class OccupancySummary {

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

    public OccupancySummary() {}

    public OccupancySummary(int totalReservations, int totalGuests, int peakOccupancy,
                            double averageUtilization, double overallUtilizationPercentage) {
        this.totalReservations = totalReservations;
        this.totalGuests = totalGuests;
        this.peakOccupancy = peakOccupancy;
        this.averageUtilization = averageUtilization;
        this.overallUtilizationPercentage = overallUtilizationPercentage;
    }

    public int getTotalReservations() {
        return totalReservations;
    }

    public void setTotalReservations(int totalReservations) {
        this.totalReservations = totalReservations;
    }

    public int getTotalGuests() {
        return totalGuests;
    }

    public void setTotalGuests(int totalGuests) {
        this.totalGuests = totalGuests;
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

    public double getOverallUtilizationPercentage() {
        return overallUtilizationPercentage;
    }

    public void setOverallUtilizationPercentage(double overallUtilizationPercentage) {
        this.overallUtilizationPercentage = overallUtilizationPercentage;
    }
}

