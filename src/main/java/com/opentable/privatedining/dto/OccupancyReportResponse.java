package com.opentable.privatedining.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Complete occupancy analytics report for a restaurant")
public class OccupancyReportResponse {

    @Schema(description = "Unique identifier of the restaurant", example = "507f1f77bcf86cd799439011")
    private String restaurantId;

    @Schema(description = "Start time of the report period", example = "2026-01-20T09:00:00")
    private LocalDateTime reportStartTime;

    @Schema(description = "End time of the report period", example = "2026-01-20T18:00:00")
    private LocalDateTime reportEndTime;

    @Schema(description = "Summary metrics for the entire report period")
    private OccupancySummary summary;

    @Schema(description = "Detailed occupancy reports per space (paginated)")
    private List<SpaceOccupancyReport> spaceReports;

    @Schema(description = "Current page number (0-based)", example = "0")
    private int page;

    @Schema(description = "Page size", example = "10")
    private int size;

    @Schema(description = "Total number of spaces", example = "5")
    private long totalElements;

    @Schema(description = "Total number of pages", example = "1")
    private int totalPages;

    public OccupancyReportResponse() {}

    public OccupancyReportResponse(String restaurantId, LocalDateTime reportStartTime, LocalDateTime reportEndTime,
                                    OccupancySummary summary, List<SpaceOccupancyReport> spaceReports,
                                    int page, int size, long totalElements, int totalPages) {
        this.restaurantId = restaurantId;
        this.reportStartTime = reportStartTime;
        this.reportEndTime = reportEndTime;
        this.summary = summary;
        this.spaceReports = spaceReports;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public LocalDateTime getReportStartTime() {
        return reportStartTime;
    }

    public void setReportStartTime(LocalDateTime reportStartTime) {
        this.reportStartTime = reportStartTime;
    }

    public LocalDateTime getReportEndTime() {
        return reportEndTime;
    }

    public void setReportEndTime(LocalDateTime reportEndTime) {
        this.reportEndTime = reportEndTime;
    }

    public OccupancySummary getSummary() {
        return summary;
    }

    public void setSummary(OccupancySummary summary) {
        this.summary = summary;
    }

    public List<SpaceOccupancyReport> getSpaceReports() {
        return spaceReports;
    }

    public void setSpaceReports(List<SpaceOccupancyReport> spaceReports) {
        this.spaceReports = spaceReports;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}

