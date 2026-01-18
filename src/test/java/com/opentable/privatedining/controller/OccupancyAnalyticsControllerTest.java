package com.opentable.privatedining.controller;

import com.opentable.privatedining.config.AnalyticsConfig;
import com.opentable.privatedining.dto.*;
import com.opentable.privatedining.exception.InvalidDateRangeException;
import com.opentable.privatedining.exception.RestaurantNotFoundException;
import com.opentable.privatedining.exception.SpaceNotFoundException;
import com.opentable.privatedining.mapper.RestaurantMapper;
import com.opentable.privatedining.mapper.SpaceMapper;
import com.opentable.privatedining.service.OccupancyAnalyticsService;
import com.opentable.privatedining.service.RestaurantService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RestaurantController.class)
class OccupancyAnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RestaurantService restaurantService;

    @MockBean
    private OccupancyAnalyticsService occupancyAnalyticsService;

    @MockBean
    private RestaurantMapper restaurantMapper;

    @MockBean
    private SpaceMapper spaceMapper;

    @MockBean
    private AnalyticsConfig analyticsConfig;

    private static final String ANALYTICS_URL = "/v1/restaurants/{id}/analytics/occupancy";

    // ==================== Success Tests ====================

    @Test
    void getOccupancyReport_WithValidParams_ShouldReturnReport() throws Exception {
        // Given
        ObjectId restaurantId = new ObjectId();
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 9, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 18, 0);

        OccupancySummary summary = new OccupancySummary(5, 25, 15, 45.5, 60.0);

        UUID spaceId = UUID.randomUUID();
        List<TimeSlotOccupancy> hourlyBreakdown = Arrays.asList(
                new TimeSlotOccupancy(startTime, startTime.plusHours(1), 2, 10, 25, 40.0),
                new TimeSlotOccupancy(startTime.plusHours(1), startTime.plusHours(2), 3, 15, 25, 60.0)
        );

        SpaceOccupancyReport spaceReport = new SpaceOccupancyReport(
                spaceId, "Garden Room", 25, 5, 15, 50.0, hourlyBreakdown);

        OccupancyReportResponse response = new OccupancyReportResponse(
                restaurantId.toHexString(), startTime, endTime, summary,
                List.of(spaceReport), 0, 10, 1, 1);

        when(occupancyAnalyticsService.generateOccupancyReport(
                eq(restaurantId), eq(startTime), eq(endTime), isNull(), eq(0), eq(10)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(get(ANALYTICS_URL, restaurantId.toHexString())
                        .param("startTime", "2026-01-20T09:00:00")
                        .param("endTime", "2026-01-20T18:00:00"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.restaurantId").value(restaurantId.toHexString()))
                .andExpect(jsonPath("$.summary.totalReservations").value(5))
                .andExpect(jsonPath("$.summary.totalGuests").value(25))
                .andExpect(jsonPath("$.summary.peakOccupancy").value(15))
                .andExpect(jsonPath("$.spaceReports.length()").value(1))
                .andExpect(jsonPath("$.spaceReports[0].spaceName").value("Garden Room"))
                .andExpect(jsonPath("$.spaceReports[0].hourlyBreakdown.length()").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getOccupancyReport_WithSpaceIdFilter_ShouldPassSpaceIdToService() throws Exception {
        // Given
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 9, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 18, 0);

        OccupancySummary summary = new OccupancySummary(3, 15, 10, 40.0, 50.0);
        SpaceOccupancyReport spaceReport = new SpaceOccupancyReport(
                spaceId, "Garden Room", 25, 3, 10, 40.0, Collections.emptyList());

        OccupancyReportResponse response = new OccupancyReportResponse(
                restaurantId.toHexString(), startTime, endTime, summary,
                List.of(spaceReport), 0, 10, 1, 1);

        when(occupancyAnalyticsService.generateOccupancyReport(
                eq(restaurantId), eq(startTime), eq(endTime), eq(spaceId), eq(0), eq(10)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(get(ANALYTICS_URL, restaurantId.toHexString())
                        .param("startTime", "2026-01-20T09:00:00")
                        .param("endTime", "2026-01-20T18:00:00")
                        .param("spaceId", spaceId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spaceReports[0].spaceId").value(spaceId.toString()));
    }

    @Test
    void getOccupancyReport_WithPaginationParams_ShouldPassPaginationToService() throws Exception {
        // Given
        ObjectId restaurantId = new ObjectId();
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 9, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 18, 0);

        OccupancySummary summary = new OccupancySummary(0, 0, 0, 0.0, 0.0);
        OccupancyReportResponse response = new OccupancyReportResponse(
                restaurantId.toHexString(), startTime, endTime, summary,
                Collections.emptyList(), 2, 5, 15, 3);

        when(occupancyAnalyticsService.generateOccupancyReport(
                eq(restaurantId), eq(startTime), eq(endTime), isNull(), eq(2), eq(5)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(get(ANALYTICS_URL, restaurantId.toHexString())
                        .param("startTime", "2026-01-20T09:00:00")
                        .param("endTime", "2026-01-20T18:00:00")
                        .param("page", "2")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    // ==================== Validation Error Tests ====================

    @Test
    void getOccupancyReport_WithMissingStartTime_ShouldReturn400() throws Exception {
        // Given
        ObjectId restaurantId = new ObjectId();

        // When & Then
        mockMvc.perform(get(ANALYTICS_URL, restaurantId.toHexString())
                        .param("endTime", "2026-01-20T18:00:00"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOccupancyReport_WithMissingEndTime_ShouldReturn400() throws Exception {
        // Given
        ObjectId restaurantId = new ObjectId();

        // When & Then
        mockMvc.perform(get(ANALYTICS_URL, restaurantId.toHexString())
                        .param("startTime", "2026-01-20T09:00:00"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOccupancyReport_WithInvalidDateFormat_ShouldReturn400() throws Exception {
        // Given
        ObjectId restaurantId = new ObjectId();

        // When & Then
        mockMvc.perform(get(ANALYTICS_URL, restaurantId.toHexString())
                        .param("startTime", "invalid-date")
                        .param("endTime", "2026-01-20T18:00:00"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOccupancyReport_WithEndTimeBeforeStartTime_ShouldReturn400() throws Exception {
        // Given
        ObjectId restaurantId = new ObjectId();
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 18, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 9, 0);

        when(occupancyAnalyticsService.generateOccupancyReport(
                eq(restaurantId), eq(startTime), eq(endTime), isNull(), eq(0), eq(10)))
                .thenThrow(new InvalidDateRangeException(startTime, endTime, "End time must be after start time"));

        // When & Then
        mockMvc.perform(get(ANALYTICS_URL, restaurantId.toHexString())
                        .param("startTime", "2026-01-20T18:00:00")
                        .param("endTime", "2026-01-20T09:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Invalid date range from 2026-01-20T18:00 to 2026-01-20T09:00: End time must be after start time"));
    }

    @Test
    void getOccupancyReport_WithRangeExceeding31Days_ShouldReturn400() throws Exception {
        // Given
        ObjectId restaurantId = new ObjectId();
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 1, 9, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 2, 5, 18, 0);

        when(occupancyAnalyticsService.generateOccupancyReport(
                eq(restaurantId), eq(startTime), eq(endTime), isNull(), eq(0), eq(10)))
                .thenThrow(new InvalidDateRangeException(startTime, endTime, 31));

        // When & Then
        mockMvc.perform(get(ANALYTICS_URL, restaurantId.toHexString())
                        .param("startTime", "2026-01-01T09:00:00")
                        .param("endTime", "2026-02-05T18:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Date range from 2026-01-01T09:00 to 2026-02-05T18:00 exceeds maximum allowed range of 31 days"));
    }

    // ==================== Not Found Tests ====================

    @Test
    void getOccupancyReport_WhenRestaurantNotFound_ShouldReturn404() throws Exception {
        // Given
        ObjectId restaurantId = new ObjectId();
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 9, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 18, 0);

        when(occupancyAnalyticsService.generateOccupancyReport(
                eq(restaurantId), eq(startTime), eq(endTime), isNull(), eq(0), eq(10)))
                .thenThrow(new RestaurantNotFoundException(restaurantId));

        // When & Then
        mockMvc.perform(get(ANALYTICS_URL, restaurantId.toHexString())
                        .param("startTime", "2026-01-20T09:00:00")
                        .param("endTime", "2026-01-20T18:00:00"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOccupancyReport_WhenSpaceNotFound_ShouldReturn404() throws Exception {
        // Given
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 9, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 18, 0);

        when(occupancyAnalyticsService.generateOccupancyReport(
                eq(restaurantId), eq(startTime), eq(endTime), eq(spaceId), eq(0), eq(10)))
                .thenThrow(new SpaceNotFoundException(spaceId));

        // When & Then
        mockMvc.perform(get(ANALYTICS_URL, restaurantId.toHexString())
                        .param("startTime", "2026-01-20T09:00:00")
                        .param("endTime", "2026-01-20T18:00:00")
                        .param("spaceId", spaceId.toString()))
                .andExpect(status().isNotFound());
    }

    // ==================== Edge Cases ====================

    @Test
    void getOccupancyReport_WithEmptyResult_ShouldReturnEmptyReport() throws Exception {
        // Given
        ObjectId restaurantId = new ObjectId();
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 9, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 18, 0);

        OccupancySummary summary = new OccupancySummary(0, 0, 0, 0.0, 0.0);
        OccupancyReportResponse response = new OccupancyReportResponse(
                restaurantId.toHexString(), startTime, endTime, summary,
                Collections.emptyList(), 0, 10, 0, 0);

        when(occupancyAnalyticsService.generateOccupancyReport(
                eq(restaurantId), eq(startTime), eq(endTime), isNull(), eq(0), eq(10)))
                .thenReturn(response);

        // When & Then
        mockMvc.perform(get(ANALYTICS_URL, restaurantId.toHexString())
                        .param("startTime", "2026-01-20T09:00:00")
                        .param("endTime", "2026-01-20T18:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.totalReservations").value(0))
                .andExpect(jsonPath("$.summary.totalGuests").value(0))
                .andExpect(jsonPath("$.spaceReports").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void getOccupancyReport_WithInvalidRestaurantIdFormat_ShouldReturn400() throws Exception {
        // When & Then
        mockMvc.perform(get(ANALYTICS_URL, "invalid-id")
                        .param("startTime", "2026-01-20T09:00:00")
                        .param("endTime", "2026-01-20T18:00:00"))
                .andExpect(status().isBadRequest());
    }
}

