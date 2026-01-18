package com.opentable.privatedining.onetime;

import com.opentable.privatedining.model.Reservation;
import com.opentable.privatedining.model.Restaurant;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Data transfer object for loading initial data from YAML configuration.
 * Contains collections of restaurants and reservations to be loaded into the database.
 */
@Getter
@Setter
public class Data {

    private List<Restaurant> restaurants;
    private List<Reservation> reservations;
}
