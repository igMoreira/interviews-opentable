package com.opentable.privatedining.service;

import com.opentable.privatedining.model.Restaurant;
import com.opentable.privatedining.model.Space;
import com.opentable.privatedining.repository.RestaurantRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class for managing restaurant operations.
 */
@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    /**
     * Constructs a new RestaurantService with the required repository.
     *
     * @param restaurantRepository the repository for restaurant data access
     */
    public RestaurantService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    /**
     * Retrieves all restaurants.
     *
     * @return list of all restaurants
     */
    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }

    /**
     * Retrieves a restaurant by its ID.
     *
     * @param id the restaurant ID
     * @return optional containing the restaurant if found
     */
    public Optional<Restaurant> getRestaurantById(ObjectId id) {
        return restaurantRepository.findById(id);
    }

    /**
     * Creates a new restaurant.
     *
     * @param restaurant the restaurant to create
     * @return the created restaurant
     */
    public Restaurant createRestaurant(Restaurant restaurant) {
        return restaurantRepository.save(restaurant);
    }

    /**
     * Updates an existing restaurant.
     *
     * @param id the restaurant ID
     * @param restaurant the updated restaurant data
     * @return optional containing the updated restaurant if found
     */
    public Optional<Restaurant> updateRestaurant(ObjectId id, Restaurant restaurant) {
        Optional<Restaurant> existingRestaurant = restaurantRepository.findById(id);
        if (existingRestaurant.isPresent()) {
            restaurant.setId(id);
            return Optional.of(restaurantRepository.save(restaurant));
        }
        return Optional.empty();
    }

    /**
     * Deletes a restaurant by its ID.
     *
     * @param id the restaurant ID
     * @return true if the restaurant was deleted, false if not found
     */
    public boolean deleteRestaurant(ObjectId id) {
        Optional<Restaurant> existingRestaurant = restaurantRepository.findById(id);
        if (existingRestaurant.isPresent()) {
            restaurantRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Adds a space to a restaurant.
     *
     * @param restaurantId the restaurant ID
     * @param space the space to add
     * @return optional containing the updated restaurant if found
     */
    public Optional<Restaurant> addSpaceToRestaurant(ObjectId restaurantId, Space space) {
        Optional<Restaurant> restaurantOpt = restaurantRepository.findById(restaurantId);
        if (restaurantOpt.isPresent()) {
            Restaurant restaurant = restaurantOpt.get();
            restaurant.getSpaces().add(space);
            return Optional.of(restaurantRepository.save(restaurant));
        }
        return Optional.empty();
    }

    /**
     * Removes a space from a restaurant.
     *
     * @param restaurantId the restaurant ID
     * @param spaceId the UUID of the space to remove
     * @return optional containing the updated restaurant if found
     */
    public Optional<Restaurant> removeSpaceFromRestaurant(ObjectId restaurantId, UUID spaceId) {
        Optional<Restaurant> restaurantOpt = restaurantRepository.findById(restaurantId);
        if (restaurantOpt.isPresent()) {
            Restaurant restaurant = restaurantOpt.get();
            restaurant.getSpaces().removeIf(space -> space.getId().equals(spaceId));
            return Optional.of(restaurantRepository.save(restaurant));
        }
        return Optional.empty();
    }

    /**
     * Retrieves a specific space from a restaurant.
     *
     * @param restaurantId the restaurant ID
     * @param spaceId the UUID of the space
     * @return optional containing the space if found
     */
    public Optional<Space> getSpaceById(ObjectId restaurantId, UUID spaceId) {
        Optional<Restaurant> restaurantOpt = restaurantRepository.findById(restaurantId);
        if (restaurantOpt.isPresent()) {
            return restaurantOpt.get().getSpaces().stream()
                    .filter(space -> space.getId().equals(spaceId))
                    .findFirst();
        }
        return Optional.empty();
    }

    /**
     * Checks if a space exists within a restaurant.
     *
     * @param restaurantId the restaurant ID
     * @param spaceId the UUID of the space
     * @return true if the space exists in the restaurant, false otherwise
     */
    public boolean spaceExistsInRestaurant(ObjectId restaurantId, UUID spaceId) {
        return getSpaceById(restaurantId, spaceId).isPresent();
    }
}