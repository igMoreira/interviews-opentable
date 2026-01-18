package com.opentable.privatedining.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents a restaurant in the private dining system.
 */
@Getter
@Setter
@Document(collection = "restaurants")
public class Restaurant {

    @Id
    private ObjectId id;
    private String name;
    private String address;
    private String cuisineType;
    private Integer capacity;
    private List<Space> spaces;

    /**
     * Default constructor that initializes an empty list of spaces.
     */
    public Restaurant() {
        this.spaces = new ArrayList<>();
    }

    /**
     * Constructs a new Restaurant with the specified details.
     *
     * @param name the name of the restaurant
     * @param address the address of the restaurant
     * @param cuisineType the type of cuisine served
     * @param capacity the total capacity of the restaurant
     */
    public Restaurant(String name, String address, String cuisineType, Integer capacity) {
        this.name = name;
        this.address = address;
        this.cuisineType = cuisineType;
        this.capacity = capacity;
        this.spaces = new ArrayList<>();
    }
}