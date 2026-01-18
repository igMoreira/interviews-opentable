package com.opentable.privatedining.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SpaceDTO {

    @Schema(description = "Unique identifier for the space", example = "123e4567-e89b-12d3-a456-426614174000", type = "string")
    private UUID id;

    @Schema(description = "Name of the space", example = "Private Dining Room A")
    private String name;

    @Schema(description = "Minimum capacity for the space", example = "2")
    private Integer minCapacity;

    @Schema(description = "Maximum capacity for the space", example = "12")
    private Integer maxCapacity;

    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "Operating start time for the space (defaults to 09:00 if not set)", example = "09:00", type = "string", pattern = "HH:mm")
    private LocalTime operatingStartTime;

    @JsonFormat(pattern = "HH:mm")
    @Schema(description = "Operating end time for the space (defaults to 22:00 if not set)", example = "22:00", type = "string", pattern = "HH:mm")
    private LocalTime operatingEndTime;

    @Schema(description = "Time slot duration in minutes for reservations (defaults to 60 if not set)", example = "60")
    private Integer timeSlotDurationMinutes;

    public SpaceDTO(String name, Integer minCapacity, Integer maxCapacity) {
        this.name = name;
        this.minCapacity = minCapacity;
        this.maxCapacity = maxCapacity;
    }

    public SpaceDTO(UUID id, String name, Integer minCapacity, Integer maxCapacity) {
        this.id = id;
        this.name = name;
        this.minCapacity = minCapacity;
        this.maxCapacity = maxCapacity;
    }
}