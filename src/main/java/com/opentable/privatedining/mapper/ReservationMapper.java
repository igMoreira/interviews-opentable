package com.opentable.privatedining.mapper;

import com.opentable.privatedining.dto.ReservationDTO;
import com.opentable.privatedining.model.Reservation;
import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * MapStruct mapper for converting between Reservation entity and ReservationDTO.
 */
@Mapper(componentModel = "spring")
public interface ReservationMapper {

    /**
     * Converts a Reservation entity to a ReservationDTO.
     *
     * @param reservation the reservation entity
     * @return the reservation DTO
     */
    @Mapping(target = "id", source = "id", qualifiedByName = "objectIdToString")
    @Mapping(target = "restaurantId", source = "restaurantId", qualifiedByName = "objectIdToString")
    ReservationDTO toDTO(Reservation reservation);

    /**
     * Converts a ReservationDTO to a Reservation entity.
     *
     * @param reservationDTO the reservation DTO
     * @return the reservation entity
     */
    @Mapping(target = "id", source = "id", qualifiedByName = "stringToObjectId")
    @Mapping(target = "restaurantId", source = "restaurantId", qualifiedByName = "stringToObjectId")
    Reservation toModel(ReservationDTO reservationDTO);

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
                // Invalid ObjectId format, return null
                return null;
            }
        }
        return null;
    }
}

