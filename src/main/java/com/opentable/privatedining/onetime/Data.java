package com.opentable.privatedining.onetime;

import com.opentable.privatedining.model.Reservation;
import com.opentable.privatedining.model.Restaurant;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Data {

    private List<Restaurant> restaurants;
    private List<Reservation> reservations;
}
