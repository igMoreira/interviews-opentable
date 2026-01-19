package com.opentable.privatedining.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Complete occupancy analytics report for a restaurant")
public class OccupancyReportDTO {

    @Schema(description = "Unique identifier of the restaurant", example = "507f1f77bcf86cd799439011")
    private String restaurantId;

    @Schema(description = "Start time of the report period", example = "2026-01-20 09:00")
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
    private LocalDateTime reportStartTime;

    @Schema(description = "End time of the report period", example = "2026-01-20 18:00")
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
    private LocalDateTime reportEndTime;

    @Schema(description = "Summary metrics for the entire report period")
    private OccupancySummaryDTO summary;

    @Schema(description = "Detailed occupancy reports per space (paginated)")
    private List<SpaceOccupancyReportDTO> spaceReports;

    @Schema(description = "Current page number (0-based)", example = "0")
    private int page;

    @Schema(description = "Page size", example = "10")
    private int size;

    @Schema(description = "Total number of spaces", example = "5")
    private long totalElements;

    @Schema(description = "Total number of pages", example = "1")
    private int totalPages;
}

