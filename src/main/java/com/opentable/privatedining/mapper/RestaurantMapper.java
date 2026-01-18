package com.opentable.privatedining.mapper;

import com.opentable.privatedining.dto.RestaurantDTO;
import com.opentable.privatedining.model.Restaurant;
import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = SpaceMapper.class)
public interface RestaurantMapper {

    @Mapping(target = "id", source = "id", qualifiedByName = "objectIdToString")
    RestaurantDTO toDTO(Restaurant restaurant);

    @Mapping(target = "id", source = "id", qualifiedByName = "stringToObjectId")
    Restaurant toModel(RestaurantDTO restaurantDTO);

    @Named("objectIdToString")
    default String objectIdToString(ObjectId objectId) {
        return objectId != null ? objectId.toString() : null;
    }

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