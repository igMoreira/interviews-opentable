package com.opentable.privatedining.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import com.opentable.privatedining.validation.DateTimeFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object for Reservation entity.
 * Used for API request and response payloads with validation annotations.
 */
@Getter
@Setter
@NoArgsConstructor
public class ReservationDTO {

    @Schema(description = "Unique identifier for the reservation", example = "507f1f77bcf86cd799439011", type = "string")
    private String id;

    @NotBlank(message = "Restaurant ID is required")
    @Schema(description = "ID of the restaurant for this reservation", example = "507f191e810c19729de860ea", type = "string")
    private String restaurantId;

    @NotNull(message = "Space ID is required")
    @Schema(description = "UUID of the specific space being reserved", example = "123e4567-e89b-12d3-a456-426614174000", type = "string")
    private UUID spaceId;

    @NotBlank(message = "Customer email is required")
    @Email(message = "Customer email must be a valid email address")
    @Schema(description = "Email address of the customer making the reservation", example = "customer@example.com")
    private String customerEmail;

    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm", message = "Start time must be in format: dd-MM-yyyy HH:mm")
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
    @Schema(type = "string", description = "Start date and time of the reservation", example = "15-01-2026 19:30", pattern = "dd-MM-yyyy HH:mm")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    @Future(message = "End time must be in the future")
    @DateTimeFormat(pattern = "dd-MM-yyyy HH:mm", message = "End time must be in format: dd-MM-yyyy HH:mm")
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm")
    @Schema(type = "string", description = "End date and time of the reservation", example = "15-01-2026 21:30", pattern = "dd-MM-yyyy HH:mm")
    private LocalDateTime endTime;

    @NotNull(message = "Party size is required")
    @Positive(message = "Party size must be a positive number")
    @Schema(description = "Number of people in the party", example = "4")
    private Integer partySize;

    @Schema(description = "Status of the reservation", example = "CONFIRMED")
    private String status;

    /**
     * Constructs a ReservationDTO with all fields.
     *
     * @param restaurantId the ID of the restaurant
     * @param spaceId the UUID of the space being reserved
     * @param customerEmail the email address of the customer
     * @param startTime the start time of the reservation
     * @param endTime the end time of the reservation
     * @param partySize the number of people in the party
     * @param status the status of the reservation
     */
    public ReservationDTO(String restaurantId, UUID spaceId, String customerEmail, LocalDateTime startTime,
        LocalDateTime endTime, Integer partySize, String status) {
        this.restaurantId = restaurantId;
        this.spaceId = spaceId;
        this.customerEmail = customerEmail;
        this.startTime = startTime;
        this.endTime = endTime;
        this.partySize = partySize;
        this.status = status;
    }
}