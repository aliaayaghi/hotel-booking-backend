package com.HotelBook.HotelBooking.HotelLocation;

import java.util.UUID;

public class LocationNotFoundException extends RuntimeException {

    public LocationNotFoundException(UUID hotelId) {
        super("Location not found for hotel: " + hotelId);
    }
}