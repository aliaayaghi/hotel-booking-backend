package com.HotelBook.HotelBooking.User.exception;


public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super("Email address is already in use: " + email);
    }
}
