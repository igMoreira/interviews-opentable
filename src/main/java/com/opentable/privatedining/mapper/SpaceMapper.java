package com.opentable.privatedining.mapper;

import com.opentable.privatedining.dto.SpaceDTO;
import com.opentable.privatedining.model.Space;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SpaceMapper {

    SpaceDTO toDTO(Space space);

    // Space ID is always generated server-side, ignore any client-provided ID
    @Mapping(target = "id", ignore = true)
    Space toModel(SpaceDTO spaceDTO);
}

