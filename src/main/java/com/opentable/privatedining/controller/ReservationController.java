package com.opentable.privatedining.controller;

import com.opentable.privatedining.dto.ReservationDTO;
import com.opentable.privatedining.mapper.ReservationMapper;
import com.opentable.privatedining.model.Reservation;
import com.opentable.privatedining.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing reservations.
 * Provides endpoints for creating, retrieving, and deleting reservations.
 */
@Validated
@RestController
@RequestMapping("/v1/reservations")
@Tag(name = "Reservation", description = "Reservation management API")
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationMapper reservationMapper;

    /**
     * Constructs a new ReservationController with the required dependencies.
     *
     * @param reservationService the service for reservation operations
     * @param reservationMapper the mapper for reservation entity/DTO conversion
     */
    public ReservationController(ReservationService reservationService, ReservationMapper reservationMapper) {
        this.reservationService = reservationService;
        this.reservationMapper = reservationMapper;
    }

    /**
     * Retrieves all reservations.
     *
     * @return list of all reservations as DTOs
     */
    @GetMapping
    @Operation(summary = "Get all reservations", description = "Retrieve a list of all reservations")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of reservations",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReservationDTO.class)))
    public List<ReservationDTO> getAllReservations() {
        return reservationService.getAllReservations()
                .stream()
                .map(reservationMapper::toDTO)
                .toList();
    }

    /**
     * Retrieves a reservation by its ID.
     *
     * @param id the reservation ID
     * @return the reservation if found, or appropriate error response
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get reservation by ID", description = "Retrieve a reservation by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reservation found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReservationDTO.class))),
            @ApiResponse(responseCode = "404", description = "Reservation not found"),
            @ApiResponse(responseCode = "400", description = "Invalid ID format")
    })
    public ResponseEntity<ReservationDTO> getReservationById(
            @Parameter(description = "ID of the reservation to retrieve", required = true)
            @PathVariable String id) {
        try {
            ObjectId objectId = new ObjectId(id);
            Optional<Reservation> reservation = reservationService.getReservationById(objectId);
            return reservation.map(r -> ResponseEntity.ok(reservationMapper.toDTO(r)))
                    .orElseGet(() -> ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Creates a new reservation.
     *
     * @param reservationDTO the reservation data to create
     * @return the created reservation with HTTP 201 status
     */
    @PostMapping
    @Operation(summary = "Create new reservation", description = "Create a new reservation in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reservation created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReservationDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid party size for the space capacity"),
            @ApiResponse(responseCode = "404", description = "Restaurant or space not found"),
            @ApiResponse(responseCode = "409", description = "Reservation time slot conflicts with existing reservation")
    })
    public ResponseEntity<ReservationDTO> createReservation(
            @Parameter(description = "Reservation object to be created", required = true)
            @Valid @RequestBody ReservationDTO reservationDTO) {
        Reservation reservation = reservationMapper.toModel(reservationDTO);
        Reservation savedReservation = reservationService.createReservation(reservation);
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationMapper.toDTO(savedReservation));
    }

    /**
     * Deletes a reservation by its ID.
     *
     * @param id the reservation ID to delete
     * @return HTTP 204 if deleted, or appropriate error response
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete reservation", description = "Delete a reservation by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Reservation deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Reservation not found"),
            @ApiResponse(responseCode = "400", description = "Invalid ID format")
    })
    public ResponseEntity<Void> deleteReservation(
            @Parameter(description = "ID of the reservation to delete", required = true)
            @PathVariable String id) {
        try {
            ObjectId objectId = new ObjectId(id);
            boolean deleted = reservationService.deleteReservation(objectId);
            return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}