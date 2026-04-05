package com.HotelBook.HotelBooking.Common;


public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
