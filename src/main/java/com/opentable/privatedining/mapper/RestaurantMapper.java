package com.opentable.privatedining.mapper;

import com.opentable.privatedining.dto.RestaurantDTO;
import com.opentable.privatedining.model.Restaurant;
import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * MapStruct mapper for converting between Restaurant entity and RestaurantDTO.
 */
@Mapper(componentModel = "spring", uses = SpaceMapper.class)
public interface RestaurantMapper {

    /**
     * Converts a Restaurant entity to a RestaurantDTO.
     *
     * @param restaurant the restaurant entity
     * @return the restaurant DTO
     */
    @Mapping(target = "id", source = "id", qualifiedByName = "objectIdToString")
    RestaurantDTO toDTO(Restaurant restaurant);

    /**
     * Converts a RestaurantDTO to a Restaurant entity.
     *
     * @param restaurantDTO the restaurant DTO
     * @return the restaurant entity
     */
    @Mapping(target = "id", source = "id", qualifiedByName = "stringToObjectId")
    Restaurant toModel(RestaurantDTO restaurantDTO);

    /**
     * Converts ObjectId to String.
     *
     * @param objectId the ObjectId
     * @return the string representation
     */
    @Named("objectIdToString")
    default String objectIdToString(ObjectId objectId) {
        return objectId != null ? objectId.toString() : null;
    }

    /**
     * Converts String to ObjectId.
     *
     * @param id the string ID
     * @return the ObjectId, or null if invalid
     */
    @Named("stringToObjectId")
    default ObjectId stringToObjectId(String id) {
        if (id != null && !id.isEmpty()) {
            try {
                return new ObjectId(id);
            } catch (IllegalArgumentException e) {
                // Invalid ObjectId format, leave it null for new entities
                return null;
            }
        }
        return null;
    }
}