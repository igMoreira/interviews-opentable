package com.opentable.privatedining.mapper;

import com.opentable.privatedining.dto.ReservationDTO;
import com.opentable.privatedining.model.Reservation;
import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    @Mapping(target = "id", source = "id", qualifiedByName = "objectIdToString")
    @Mapping(target = "restaurantId", source = "restaurantId", qualifiedByName = "objectIdToString")
    ReservationDTO toDTO(Reservation reservation);

    @Mapping(target = "id", source = "id", qualifiedByName = "stringToObjectId")
    @Mapping(target = "restaurantId", source = "restaurantId", qualifiedByName = "stringToObjectId")
    Reservation toModel(ReservationDTO reservationDTO);

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
                // Invalid ObjectId format, return null
                return null;
            }
        }
        return null;
    }
}

