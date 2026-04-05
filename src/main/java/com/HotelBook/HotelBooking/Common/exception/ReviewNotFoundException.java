package com.HotelBook.HotelBooking.Common.exception;


import org.springframework.http.HttpStatus;

public class ReviewNotFoundException extends BusinessException {
    public ReviewNotFoundException(Long id) {
        super("REVIEW_NOT_FOUND",
                "Review with id " + id + " was not found",
                HttpStatus.NOT_FOUND);
    }
}