package com.opentable.privatedining.mapper;

import com.opentable.privatedining.dto.ReservationDTO;
import com.opentable.privatedining.model.Reservation;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ReservationMapperTest {

    private ReservationMapper reservationMapper;

    @BeforeEach
    void setUp() {
        reservationMapper = new ReservationMapperImpl();
    }

    // ==================== toDTO Tests ====================

    @Test
    void toDTO_WhenReservationIsNull_ShouldReturnNull() {
        // When
        ReservationDTO result = reservationMapper.toDTO(null);

        // Then
        assertNull(result);
    }

    @Test
    void toDTO_WhenReservationIsValid_ShouldMapAllFields() {
        // Given
        ObjectId id = new ObjectId();
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 12, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 14, 0);

        Reservation reservation = new Reservation();
        reservation.setId(id);
        reservation.setRestaurantId(restaurantId);
        reservation.setSpaceId(spaceId);
        reservation.setCustomerEmail("test@example.com");
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);
        reservation.setPartySize(4);
        reservation.setStatus("CONFIRMED");

        // When
        ReservationDTO result = reservationMapper.toDTO(reservation);

        // Then
        assertNotNull(result);
        assertEquals(id.toHexString(), result.getId());
        assertEquals(restaurantId.toHexString(), result.getRestaurantId());
        assertEquals(spaceId, result.getSpaceId());
        assertEquals("test@example.com", result.getCustomerEmail());
        assertEquals(startTime, result.getStartTime());
        assertEquals(endTime, result.getEndTime());
        assertEquals(4, result.getPartySize());
        assertEquals("CONFIRMED", result.getStatus());
    }

    @Test
    void toDTO_WhenIdsAreNull_ShouldMapWithNullIds() {
        // Given
        Reservation reservation = new Reservation();
        reservation.setId(null);
        reservation.setRestaurantId(null);
        reservation.setCustomerEmail("test@example.com");
        reservation.setPartySize(4);

        // When
        ReservationDTO result = reservationMapper.toDTO(reservation);

        // Then
        assertNotNull(result);
        assertNull(result.getId());
        assertNull(result.getRestaurantId());
        assertEquals("test@example.com", result.getCustomerEmail());
    }

    // ==================== toModel Tests ====================

    @Test
    void toModel_WhenDTOIsNull_ShouldReturnNull() {
        // When
        Reservation result = reservationMapper.toModel(null);

        // Then
        assertNull(result);
    }

    @Test
    void toModel_WhenDTOIsValid_ShouldMapAllFields() {
        // Given
        ObjectId id = new ObjectId();
        ObjectId restaurantId = new ObjectId();
        UUID spaceId = UUID.randomUUID();
        LocalDateTime startTime = LocalDateTime.of(2026, 1, 20, 12, 0);
        LocalDateTime endTime = LocalDateTime.of(2026, 1, 20, 14, 0);

        ReservationDTO dto = new ReservationDTO();
        dto.setId(id.toHexString());
        dto.setRestaurantId(restaurantId.toHexString());
        dto.setSpaceId(spaceId);
        dto.setCustomerEmail("test@example.com");
        dto.setStartTime(startTime);
        dto.setEndTime(endTime);
        dto.setPartySize(4);
        dto.setStatus("CONFIRMED");

        // When
        Reservation result = reservationMapper.toModel(dto);

        // Then
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals(restaurantId, result.getRestaurantId());
        assertEquals(spaceId, result.getSpaceId());
        assertEquals("test@example.com", result.getCustomerEmail());
        assertEquals(startTime, result.getStartTime());
        assertEquals(endTime, result.getEndTime());
        assertEquals(4, result.getPartySize());
        assertEquals("CONFIRMED", result.getStatus());
    }

    @Test
    void toModel_WhenIdsAreNull_ShouldMapWithNullIds() {
        // Given
        ReservationDTO dto = new ReservationDTO();
        dto.setId(null);
        dto.setRestaurantId(null);
        dto.setCustomerEmail("test@example.com");
        dto.setPartySize(4);

        // When
        Reservation result = reservationMapper.toModel(dto);

        // Then
        assertNotNull(result);
        assertNull(result.getId());
        assertNull(result.getRestaurantId());
        assertEquals("test@example.com", result.getCustomerEmail());
    }

    @Test
    void toModel_WhenIdsAreEmpty_ShouldMapWithNullIds() {
        // Given
        ReservationDTO dto = new ReservationDTO();
        dto.setId("");
        dto.setRestaurantId("");
        dto.setCustomerEmail("test@example.com");
        dto.setPartySize(4);

        // When
        Reservation result = reservationMapper.toModel(dto);

        // Then
        assertNotNull(result);
        assertNull(result.getId());
        assertNull(result.getRestaurantId());
    }

    @Test
    void toModel_WhenIdIsInvalidObjectId_ShouldMapWithNullId() {
        // Given
        ReservationDTO dto = new ReservationDTO();
        dto.setId("invalid-object-id");
        dto.setRestaurantId("also-invalid");
        dto.setCustomerEmail("test@example.com");
        dto.setPartySize(4);

        // When
        Reservation result = reservationMapper.toModel(dto);

        // Then
        assertNotNull(result);
        assertNull(result.getId());
        assertNull(result.getRestaurantId());
    }

    // ==================== ObjectId Conversion Tests ====================

    @Test
    void objectIdToString_WhenObjectIdIsNull_ShouldReturnNull() {
        // When
        String result = reservationMapper.objectIdToString(null);

        // Then
        assertNull(result);
    }

    @Test
    void objectIdToString_WhenObjectIdIsValid_ShouldReturnHexString() {
        // Given
        ObjectId objectId = new ObjectId();

        // When
        String result = reservationMapper.objectIdToString(objectId);

        // Then
        assertNotNull(result);
        assertEquals(objectId.toHexString(), result);
    }

    @Test
    void stringToObjectId_WhenStringIsNull_ShouldReturnNull() {
        // When
        ObjectId result = reservationMapper.stringToObjectId(null);

        // Then
        assertNull(result);
    }

    @Test
    void stringToObjectId_WhenStringIsEmpty_ShouldReturnNull() {
        // When
        ObjectId result = reservationMapper.stringToObjectId("");

        // Then
        assertNull(result);
    }

    @Test
    void stringToObjectId_WhenStringIsValidObjectId_ShouldReturnObjectId() {
        // Given
        ObjectId expected = new ObjectId();
        String hexString = expected.toHexString();

        // When
        ObjectId result = reservationMapper.stringToObjectId(hexString);

        // Then
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    void stringToObjectId_WhenStringIsInvalidObjectId_ShouldReturnNull() {
        // When
        ObjectId result = reservationMapper.stringToObjectId("invalid-id");

        // Then
        assertNull(result);
    }
}

