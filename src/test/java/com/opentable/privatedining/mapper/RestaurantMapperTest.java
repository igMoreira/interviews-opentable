package com.opentable.privatedining.mapper;

import com.opentable.privatedining.dto.RestaurantDTO;
import com.opentable.privatedining.dto.SpaceDTO;
import com.opentable.privatedining.model.Restaurant;
import com.opentable.privatedining.model.Space;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RestaurantMapperTest {

    @Spy
    private SpaceMapperImpl spaceMapper;

    @InjectMocks
    private RestaurantMapperImpl restaurantMapper;

    @BeforeEach
    void setUp() {
        // SpaceMapper is injected via @InjectMocks
    }

    // ==================== toDTO Tests ====================

    @Test
    void toDTO_WhenRestaurantIsNull_ShouldReturnNull() {
        // When
        RestaurantDTO result = restaurantMapper.toDTO(null);

        // Then
        assertNull(result);
    }

    @Test
    void toDTO_WhenRestaurantIsValid_ShouldMapAllFields() {
        // Given
        ObjectId id = new ObjectId();
        Restaurant restaurant = new Restaurant("The French Laundry", "123 Main St", "French", 100);
        restaurant.setId(id);

        Space space1 = new Space("Garden Room", 5, 25);
        Space space2 = new Space("Wine Cellar", 2, 15);
        restaurant.setSpaces(Arrays.asList(space1, space2));

        // When
        RestaurantDTO result = restaurantMapper.toDTO(restaurant);

        // Then
        assertNotNull(result);
        assertEquals(id.toHexString(), result.getId());
        assertEquals("The French Laundry", result.getName());
        assertEquals("123 Main St", result.getAddress());
        assertEquals("French", result.getCuisineType());
        assertEquals(100, result.getCapacity());
        assertNotNull(result.getSpaces());
        assertEquals(2, result.getSpaces().size());
        assertEquals("Garden Room", result.getSpaces().get(0).getName());
        assertEquals("Wine Cellar", result.getSpaces().get(1).getName());
    }

    @Test
    void toDTO_WhenIdIsNull_ShouldMapWithNullId() {
        // Given
        Restaurant restaurant = new Restaurant("Test Restaurant", "Address", "Cuisine", 50);
        restaurant.setId(null);

        // When
        RestaurantDTO result = restaurantMapper.toDTO(restaurant);

        // Then
        assertNotNull(result);
        assertNull(result.getId());
        assertEquals("Test Restaurant", result.getName());
    }

    @Test
    void toDTO_WhenSpacesListIsNull_ShouldMapWithNullSpaces() {
        // Given
        Restaurant restaurant = new Restaurant();
        restaurant.setId(new ObjectId());
        restaurant.setName("Test Restaurant");
        restaurant.setSpaces(null);

        // When
        RestaurantDTO result = restaurantMapper.toDTO(restaurant);

        // Then
        assertNotNull(result);
        assertNull(result.getSpaces());
    }

    @Test
    void toDTO_WhenSpacesListIsEmpty_ShouldMapWithEmptySpaces() {
        // Given
        Restaurant restaurant = new Restaurant("Test Restaurant", "Address", "Cuisine", 50);
        restaurant.setSpaces(List.of());

        // When
        RestaurantDTO result = restaurantMapper.toDTO(restaurant);

        // Then
        assertNotNull(result);
        assertNotNull(result.getSpaces());
        assertTrue(result.getSpaces().isEmpty());
    }

    // ==================== toModel Tests ====================

    @Test
    void toModel_WhenDTOIsNull_ShouldReturnNull() {
        // When
        Restaurant result = restaurantMapper.toModel(null);

        // Then
        assertNull(result);
    }

    @Test
    void toModel_WhenDTOIsValid_ShouldMapAllFields() {
        // Given
        ObjectId id = new ObjectId();
        RestaurantDTO dto = new RestaurantDTO("Test Restaurant", "123 Main St", "Italian", 80);
        dto.setId(id.toHexString());

        SpaceDTO spaceDTO1 = new SpaceDTO("Garden Room", 5, 25);
        SpaceDTO spaceDTO2 = new SpaceDTO("Wine Cellar", 2, 15);
        dto.setSpaces(Arrays.asList(spaceDTO1, spaceDTO2));

        // When
        Restaurant result = restaurantMapper.toModel(dto);

        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Test Restaurant", result.getName());
        assertEquals("123 Main St", result.getAddress());
        assertEquals("Italian", result.getCuisineType());
        assertEquals(80, result.getCapacity());
        assertNotNull(result.getSpaces());
        assertEquals(2, result.getSpaces().size());
        assertEquals("Garden Room", result.getSpaces().get(0).getName());
        assertEquals("Wine Cellar", result.getSpaces().get(1).getName());
    }

    @Test
    void toModel_WhenIdIsNull_ShouldMapWithNullId() {
        // Given
        RestaurantDTO dto = new RestaurantDTO("Test Restaurant", "Address", "Cuisine", 50);
        dto.setId(null);

        // When
        Restaurant result = restaurantMapper.toModel(dto);

        // Then
        assertNotNull(result);
        assertNull(result.getId());
        assertEquals("Test Restaurant", result.getName());
    }

    @Test
    void toModel_WhenIdIsEmpty_ShouldMapWithNullId() {
        // Given
        RestaurantDTO dto = new RestaurantDTO("Test Restaurant", "Address", "Cuisine", 50);
        dto.setId("");

        // When
        Restaurant result = restaurantMapper.toModel(dto);

        // Then
        assertNotNull(result);
        assertNull(result.getId());
    }

    @Test
    void toModel_WhenIdIsInvalid_ShouldMapWithNullId() {
        // Given
        RestaurantDTO dto = new RestaurantDTO("Test Restaurant", "Address", "Cuisine", 50);
        dto.setId("invalid-object-id");

        // When
        Restaurant result = restaurantMapper.toModel(dto);

        // Then
        assertNotNull(result);
        assertNull(result.getId());
    }

    @Test
    void toModel_WhenSpacesListIsNull_ShouldMapWithNullSpaces() {
        // Given
        RestaurantDTO dto = new RestaurantDTO();
        dto.setId(new ObjectId().toHexString());
        dto.setName("Test Restaurant");
        dto.setSpaces(null);

        // When
        Restaurant result = restaurantMapper.toModel(dto);

        // Then
        assertNotNull(result);
        assertNull(result.getSpaces());
    }

    @Test
    void toModel_WhenSpacesListIsEmpty_ShouldMapWithEmptySpaces() {
        // Given
        RestaurantDTO dto = new RestaurantDTO("Test Restaurant", "Address", "Cuisine", 50);
        dto.setSpaces(List.of());

        // When
        Restaurant result = restaurantMapper.toModel(dto);

        // Then
        assertNotNull(result);
        assertNotNull(result.getSpaces());
        assertTrue(result.getSpaces().isEmpty());
    }

    // ==================== ObjectId Conversion Tests ====================

    @Test
    void objectIdToString_WhenObjectIdIsNull_ShouldReturnNull() {
        // When
        String result = restaurantMapper.objectIdToString(null);

        // Then
        assertNull(result);
    }

    @Test
    void objectIdToString_WhenObjectIdIsValid_ShouldReturnHexString() {
        // Given
        ObjectId objectId = new ObjectId();

        // When
        String result = restaurantMapper.objectIdToString(objectId);

        // Then
        assertNotNull(result);
        assertEquals(objectId.toHexString(), result);
    }

    @Test
    void stringToObjectId_WhenStringIsNull_ShouldReturnNull() {
        // When
        ObjectId result = restaurantMapper.stringToObjectId(null);

        // Then
        assertNull(result);
    }

    @Test
    void stringToObjectId_WhenStringIsEmpty_ShouldReturnNull() {
        // When
        ObjectId result = restaurantMapper.stringToObjectId("");

        // Then
        assertNull(result);
    }

    @Test
    void stringToObjectId_WhenStringIsValidObjectId_ShouldReturnObjectId() {
        // Given
        ObjectId expected = new ObjectId();
        String hexString = expected.toHexString();

        // When
        ObjectId result = restaurantMapper.stringToObjectId(hexString);

        // Then
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    void stringToObjectId_WhenStringIsInvalidObjectId_ShouldReturnNull() {
        // When
        ObjectId result = restaurantMapper.stringToObjectId("invalid-id");

        // Then
        assertNull(result);
    }
}

