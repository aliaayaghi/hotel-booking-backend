package com.HotelBook.HotelBooking.Common.exception;


import org.springframework.http.HttpStatus;

import java.util.UUID;

public class ReviewNotFoundException extends BusinessException {
    public ReviewNotFoundException(UUID id) {
        super("REVIEW_NOT_FOUND",
                "Review with id " + id + " was not found",
                HttpStatus.NOT_FOUND);
    }
}