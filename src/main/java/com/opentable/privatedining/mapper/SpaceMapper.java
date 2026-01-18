package com.opentable.privatedining.mapper;

import com.opentable.privatedining.dto.SpaceDTO;
import com.opentable.privatedining.model.Space;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between Space entity and SpaceDTO.
 */
@Mapper(componentModel = "spring")
public interface SpaceMapper {

    /**
     * Converts a Space entity to a SpaceDTO.
     *
     * @param space the space entity
     * @return the space DTO
     */
    SpaceDTO toDTO(Space space);

    /**
     * Converts a SpaceDTO to a Space entity.
     * Space ID is always generated server-side, so any client-provided ID is ignored.
     *
     * @param spaceDTO the space DTO
     * @return the space entity
     */
    // Space ID is always generated server-side, ignore any client-provided ID
    @Mapping(target = "id", ignore = true)
    Space toModel(SpaceDTO spaceDTO);
}

