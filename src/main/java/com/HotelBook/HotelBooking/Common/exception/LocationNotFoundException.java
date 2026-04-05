package com.HotelBook.HotelBooking.Common.exception;

import java.util.UUID;

public class LocationNotFoundException extends RuntimeException {

    public LocationNotFoundException(UUID hotelId) {
        super("Location not found for hotel: " + hotelId);
    }
}