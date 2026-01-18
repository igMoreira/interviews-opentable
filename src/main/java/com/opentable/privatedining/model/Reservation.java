package com.opentable.privatedining.model;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents a reservation in the private dining system.
 */
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "reservations")
public class Reservation {

    @Id
    private transient ObjectId id;
    private ObjectId restaurantId;
    private UUID spaceId;
    private String customerEmail;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer partySize;
    private String status;

    /**
     * Constructs a new Reservation with the specified details.
     *
     * @param restaurantId the ID of the restaurant
     * @param spaceId the UUID of the space being reserved
     * @param customerEmail the email address of the customer
     * @param startTime the start time of the reservation
     * @param endTime the end time of the reservation
     * @param partySize the number of people in the party
     * @param status the status of the reservation
     */
    public Reservation(ObjectId restaurantId, UUID spaceId, String customerEmail, LocalDateTime startTime,
        LocalDateTime endTime, Integer partySize, String status) {
        this.restaurantId = restaurantId;
        this.spaceId = spaceId;
        this.customerEmail = customerEmail;
        this.startTime = startTime;
        this.endTime = endTime;
        this.partySize = partySize;
        this.status = status;
    }
}