package com.opentable.privatedining.controller;

import com.opentable.privatedining.dto.OccupancyReportResponse;
import com.opentable.privatedining.dto.RestaurantDTO;
import com.opentable.privatedining.dto.SpaceDTO;
import com.opentable.privatedining.mapper.RestaurantMapper;
import com.opentable.privatedining.mapper.SpaceMapper;
import com.opentable.privatedining.model.Restaurant;
import com.opentable.privatedining.model.Space;
import com.opentable.privatedining.service.OccupancyAnalyticsService;
import com.opentable.privatedining.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.bson.types.ObjectId;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

//TODO: No validation is done on the input data, consider adding validation annotations and handling
@RestController
@RequestMapping("/v1/restaurants")
@Tag(name = "Restaurant", description = "Restaurant management API")
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final OccupancyAnalyticsService occupancyAnalyticsService;
    private final RestaurantMapper restaurantMapper;
    private final SpaceMapper spaceMapper;

    public RestaurantController(RestaurantService restaurantService,
                                OccupancyAnalyticsService occupancyAnalyticsService,
                                RestaurantMapper restaurantMapper,
                                SpaceMapper spaceMapper) {
        this.restaurantService = restaurantService;
        this.occupancyAnalyticsService = occupancyAnalyticsService;
        this.restaurantMapper = restaurantMapper;
        this.spaceMapper = spaceMapper;
    }

    @GetMapping
    @Operation(summary = "Get all restaurants", description = "Retrieve a list of all restaurants")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of restaurants",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RestaurantDTO.class)))
    public List<RestaurantDTO> getAllRestaurants() {
        return restaurantService.getAllRestaurants()
                .stream()
                .map(restaurantMapper::toDTO)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get restaurant by ID", description = "Retrieve a restaurant by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Restaurant found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RestaurantDTO.class))),
            @ApiResponse(responseCode = "404", description = "Restaurant not found")
    })
    public ResponseEntity<RestaurantDTO> getRestaurantById(
            @Parameter(description = "ID of the restaurant to retrieve", required = true)
            @PathVariable String id) {
        try {
            ObjectId objectId = new ObjectId(id);
            Optional<Restaurant> restaurant = restaurantService.getRestaurantById(objectId);
            return restaurant.map(r -> ResponseEntity.ok(restaurantMapper.toDTO(r)))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    @Operation(summary = "Create new restaurant", description = "Create a new restaurant in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Restaurant created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RestaurantDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<RestaurantDTO> createRestaurant(
            @Parameter(description = "Restaurant object to be created", required = true)
            @RequestBody RestaurantDTO restaurantDTO) {
        Restaurant restaurant = restaurantMapper.toModel(restaurantDTO);
        Restaurant savedRestaurant = restaurantService.createRestaurant(restaurant);
        return ResponseEntity.status(HttpStatus.CREATED).body(restaurantMapper.toDTO(savedRestaurant));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update restaurant", description = "Update an existing restaurant by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Restaurant updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RestaurantDTO.class))),
            @ApiResponse(responseCode = "404", description = "Restaurant not found"),
            @ApiResponse(responseCode = "400", description = "Invalid ID format")
    })
    public ResponseEntity<RestaurantDTO> updateRestaurant(
            @Parameter(description = "ID of the restaurant to update", required = true)
            @PathVariable String id,
            @Parameter(description = "Updated restaurant object", required = true)
            @RequestBody RestaurantDTO restaurantDTO) {
        try {
            ObjectId objectId = new ObjectId(id);
            Restaurant restaurant = restaurantMapper.toModel(restaurantDTO);
            Optional<Restaurant> updatedRestaurant = restaurantService.updateRestaurant(objectId, restaurant);
            return updatedRestaurant.map(r -> ResponseEntity.ok(restaurantMapper.toDTO(r)))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete restaurant", description = "Delete a restaurant by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Restaurant deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Restaurant not found"),
            @ApiResponse(responseCode = "400", description = "Invalid ID format")
    })
    public ResponseEntity<Void> deleteRestaurant(
            @Parameter(description = "ID of the restaurant to delete", required = true)
            @PathVariable String id) {
        try {
            ObjectId objectId = new ObjectId(id);
            boolean deleted = restaurantService.deleteRestaurant(objectId);
            return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/spaces")
    @Operation(summary = "Add space to restaurant", description = "Add a new space to a restaurant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Space added successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RestaurantDTO.class))),
            @ApiResponse(responseCode = "404", description = "Restaurant not found"),
            @ApiResponse(responseCode = "400", description = "Invalid ID format")
    })
    public ResponseEntity<RestaurantDTO> addSpaceToRestaurant(
            @Parameter(description = "ID of the restaurant", required = true)
            @PathVariable String id,
            @Parameter(description = "Space object to be added", required = true)
            @RequestBody SpaceDTO spaceDTO) {
        try {
            ObjectId objectId = new ObjectId(id);
            Space space = spaceMapper.toModel(spaceDTO);
            Optional<Restaurant> updatedRestaurant = restaurantService.addSpaceToRestaurant(objectId, space);
            return updatedRestaurant.map(r -> ResponseEntity.ok(restaurantMapper.toDTO(r)))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}/spaces/{spaceId}")
    @Operation(summary = "Remove space from restaurant", description = "Remove a space from a restaurant")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Space removed successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = RestaurantDTO.class))),
            @ApiResponse(responseCode = "404", description = "Restaurant or space not found"),
            @ApiResponse(responseCode = "400", description = "Invalid ID format")
    })
    public ResponseEntity<RestaurantDTO> removeSpaceFromRestaurant(
            @Parameter(description = "ID of the restaurant", required = true)
            @PathVariable String id,
            @Parameter(description = "UUID of the space to remove", required = true)
            @PathVariable String spaceId) {
        try {
            ObjectId objectId = new ObjectId(id);
            UUID spaceUuid = UUID.fromString(spaceId);
            Optional<Restaurant> updatedRestaurant = restaurantService.removeSpaceFromRestaurant(objectId, spaceUuid);
            return updatedRestaurant.map(r -> ResponseEntity.ok(restaurantMapper.toDTO(r)))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}/analytics/occupancy")
    @Operation(summary = "Get occupancy analytics report",
            description = "Generate an analytical report of occupancy levels for a restaurant within a specified date/time range. " +
                    "Returns hourly breakdown of occupancy per space with utilization metrics.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Occupancy report generated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OccupancyReportResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid parameters (invalid date range, exceeds max 31 days, or invalid ID format)"),
            @ApiResponse(responseCode = "404", description = "Restaurant or space not found")
    })
    public ResponseEntity<OccupancyReportResponse> getOccupancyReport(
            @Parameter(description = "ID of the restaurant", required = true)
            @PathVariable String id,
            @Parameter(description = "Start of the report period (ISO date-time format)", required = true, example = "2026-01-20T09:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "End of the report period (ISO date-time format)", required = true, example = "2026-01-20T18:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @Parameter(description = "Optional space ID to filter the report to a single space")
            @RequestParam(required = false) UUID spaceId,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        ObjectId restaurantId = new ObjectId(id);
        OccupancyReportResponse report = occupancyAnalyticsService.generateOccupancyReport(
                restaurantId, startTime, endTime, spaceId, page, size);
        return ResponseEntity.ok(report);
    }
}