package com.opentable.privatedining.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestaurantDTO {

    @Schema(description = "Unique identifier for the restaurant", example = "507f1f77bcf86cd799439011", type = "string")
    private String id;

    @NotBlank(message = "Restaurant name is required")
    @Size(min = 1, max = 100, message = "Restaurant name must be between 1 and 100 characters")
    @Schema(description = "Name of the restaurant", example = "The French Laundry")
    private String name;

    @NotBlank(message = "Address is required")
    @Size(max = 255, message = "Address must not exceed 255 characters")
    @Schema(description = "Address of the restaurant", example = "6640 Washington St, Yountville, CA 94599")
    private String address;

    @NotBlank(message = "Cuisine type is required")
    @Size(max = 50, message = "Cuisine type must not exceed 50 characters")
    @Schema(description = "Type of cuisine served", example = "French")
    private String cuisineType;

    @NotNull(message = "Capacity is required")
    @Positive(message = "Capacity must be a positive number")
    @Schema(description = "Total capacity of the restaurant", example = "100")
    private Integer capacity;

    @Valid
    @Schema(description = "List of spaces available in the restaurant")
    private List<SpaceDTO> spaces;

    public RestaurantDTO() {
        this.spaces = new ArrayList<>();
    }

    public RestaurantDTO(String name, String address, String cuisineType, Integer capacity) {
        this.name = name;
        this.address = address;
        this.cuisineType = cuisineType;
        this.capacity = capacity;
        this.spaces = new ArrayList<>();
    }

    public RestaurantDTO(String id, String name, String address, String cuisineType, Integer capacity,
        List<SpaceDTO> spaces) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.cuisineType = cuisineType;
        this.capacity = capacity;
        this.spaces = spaces != null ? spaces : new ArrayList<>();
    }
}