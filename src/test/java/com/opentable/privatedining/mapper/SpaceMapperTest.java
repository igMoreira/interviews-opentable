package com.opentable.privatedining.mapper;

import com.opentable.privatedining.dto.SpaceDTO;
import com.opentable.privatedining.model.Space;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SpaceMapperTest {

    private SpaceMapper spaceMapper;

    @BeforeEach
    void setUp() {
        spaceMapper = new SpaceMapperImpl();
    }

    // ==================== toDTO Tests ====================

    @Test
    void toDTO_WhenSpaceIsNull_ShouldReturnNull() {
        // When
        SpaceDTO result = spaceMapper.toDTO(null);

        // Then
        assertNull(result);
    }

    @Test
    void toDTO_WhenSpaceIsValid_ShouldMapAllFields() {
        // Given
        UUID id = UUID.randomUUID();
        LocalTime operatingStartTime = LocalTime.of(10, 0);
        LocalTime operatingEndTime = LocalTime.of(22, 0);

        Space space = new Space();
        space.setId(id);
        space.setName("Garden Room");
        space.setMinCapacity(2);
        space.setMaxCapacity(20);
        space.setOperatingStartTime(operatingStartTime);
        space.setOperatingEndTime(operatingEndTime);
        space.setTimeSlotDurationMinutes(30);

        // When
        SpaceDTO result = spaceMapper.toDTO(space);

        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Garden Room", result.getName());
        assertEquals(2, result.getMinCapacity());
        assertEquals(20, result.getMaxCapacity());
        assertEquals(operatingStartTime, result.getOperatingStartTime());
        assertEquals(operatingEndTime, result.getOperatingEndTime());
        assertEquals(30, result.getTimeSlotDurationMinutes());
    }

    @Test
    void toDTO_WhenSpaceHasDefaultValues_ShouldMapWithDefaults() {
        // Given
        Space space = new Space("Test Room", 5, 15);

        // When
        SpaceDTO result = spaceMapper.toDTO(space);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId()); // UUID is auto-generated
        assertEquals("Test Room", result.getName());
        assertEquals(5, result.getMinCapacity());
        assertEquals(15, result.getMaxCapacity());
        // Operating times use defaults from SpaceDefaultsConfig
        assertNotNull(result.getOperatingStartTime());
        assertNotNull(result.getOperatingEndTime());
        assertNotNull(result.getTimeSlotDurationMinutes());
    }

    // ==================== toModel Tests ====================

    @Test
    void toModel_WhenDTOIsNull_ShouldReturnNull() {
        // When
        Space result = spaceMapper.toModel(null);

        // Then
        assertNull(result);
    }

    @Test
    void toModel_WhenDTOIsValid_ShouldMapAllFieldsExceptId() {
        // Given
        UUID id = UUID.randomUUID();
        LocalTime operatingStartTime = LocalTime.of(10, 0);
        LocalTime operatingEndTime = LocalTime.of(22, 0);

        SpaceDTO dto = new SpaceDTO();
        dto.setId(id);
        dto.setName("Garden Room");
        dto.setMinCapacity(2);
        dto.setMaxCapacity(20);
        dto.setOperatingStartTime(operatingStartTime);
        dto.setOperatingEndTime(operatingEndTime);
        dto.setTimeSlotDurationMinutes(30);

        // When
        Space result = spaceMapper.toModel(dto);

        // Then
        assertNotNull(result);
        // ID is ignored as per @Mapping annotation
        assertNotNull(result.getId()); // Gets a new UUID from Space constructor
        assertNotEquals(id, result.getId()); // Should be different - ID is ignored
        assertEquals("Garden Room", result.getName());
        assertEquals(2, result.getMinCapacity());
        assertEquals(20, result.getMaxCapacity());
        assertEquals(operatingStartTime, result.getOperatingStartTime());
        assertEquals(operatingEndTime, result.getOperatingEndTime());
        assertEquals(30, result.getTimeSlotDurationMinutes());
    }

    @Test
    void toModel_WhenDTOHasNullValues_ShouldMapWithNulls() {
        // Given
        SpaceDTO dto = new SpaceDTO();
        dto.setName("Test Room");
        dto.setMinCapacity(5);
        dto.setMaxCapacity(15);
        // Operating times are null

        // When
        Space result = spaceMapper.toModel(dto);

        // Then
        assertNotNull(result);
        assertEquals("Test Room", result.getName());
        assertEquals(5, result.getMinCapacity());
        assertEquals(15, result.getMaxCapacity());
        // When null, Space uses defaults from SpaceDefaultsConfig
        assertNotNull(result.getOperatingStartTime());
        assertNotNull(result.getOperatingEndTime());
        assertNotNull(result.getTimeSlotDurationMinutes());
    }

    @Test
    void toModel_ShouldIgnoreClientProvidedId() {
        // Given - DTO with a specific ID that should be ignored
        UUID clientProvidedId = UUID.randomUUID();
        SpaceDTO dto = new SpaceDTO(clientProvidedId, "Test Room", 5, 15);

        // When
        Space result = spaceMapper.toModel(dto);

        // Then
        assertNotNull(result);
        // The client-provided ID should be ignored, and a new one generated
        assertNotNull(result.getId());
        assertNotEquals(clientProvidedId, result.getId());
    }
}

