package com.opentable.privatedining.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.opentable.privatedining.validation.TimeFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object for Space entity.
 * Used for API request and response payloads with validation annotations.
 */
@Getter
@Setter
@NoArgsConstructor
public class SpaceDTO {

    @Schema(description = "Unique identifier for the space", example = "123e4567-e89b-12d3-a456-426614174000", type = "string")
    private UUID id;

    @NotBlank(message = "Space name is required")
    @Size(min = 1, max = 100, message = "Space name must be between 1 and 100 characters")
    @Schema(description = "Name of the space", example = "Private Dining Room A")
    private String name;

    @NotNull(message = "Minimum capacity is required")
    @Positive(message = "Minimum capacity must be a positive number")
    @Schema(description = "Minimum capacity for the space", example = "2")
    private Integer minCapacity;

    @NotNull(message = "Maximum capacity is required")
    @Positive(message = "Maximum capacity must be a positive number")
    @Schema(description = "Maximum capacity for the space", example = "12")
    private Integer maxCapacity;

    @TimeFormat(pattern = "HH:mm", message = "Operating start time must be in format: HH:mm")
    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "Operating start time for the space (defaults to 09:00 if not set)", example = "09:00", type = "string", pattern = "HH:mm")
    private LocalTime operatingStartTime;

    @TimeFormat(pattern = "HH:mm", message = "Operating end time must be in format: HH:mm")
    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "Operating end time for the space (defaults to 22:00 if not set)", example = "22:00", type = "string", pattern = "HH:mm")
    private LocalTime operatingEndTime;

    @Positive(message = "Time slot duration must be a positive number")
    @Schema(description = "Time slot duration in minutes for reservations (defaults to 60 if not set)", example = "60")
    private Integer timeSlotDurationMinutes;

    /**
     * Constructs a SpaceDTO with basic details.
     *
     * @param name the name of the space
     * @param minCapacity the minimum capacity
     * @param maxCapacity the maximum capacity
     */
    public SpaceDTO(String name, Integer minCapacity, Integer maxCapacity) {
        this.name = name;
        this.minCapacity = minCapacity;
        this.maxCapacity = maxCapacity;
    }

    /**
     * Constructs a SpaceDTO with ID and basic details.
     *
     * @param id the unique identifier
     * @param name the name of the space
     * @param minCapacity the minimum capacity
     * @param maxCapacity the maximum capacity
     */
    public SpaceDTO(UUID id, String name, Integer minCapacity, Integer maxCapacity) {
        this.id = id;
        this.name = name;
        this.minCapacity = minCapacity;
        this.maxCapacity = maxCapacity;
    }
}