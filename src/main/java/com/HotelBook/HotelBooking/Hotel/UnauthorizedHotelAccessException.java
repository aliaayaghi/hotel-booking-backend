package com.HotelBook.HotelBooking.Hotel;

import java.util.UUID;

public class UnauthorizedHotelAccessException extends RuntimeException {

    public UnauthorizedHotelAccessException(UUID hotelId) {
        super("You do not have permission to access hotel: " + hotelId);
    }
}