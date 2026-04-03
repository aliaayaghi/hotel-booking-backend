package com.HotelBook.HotelBooking.catalog.customer;

import java.util.List;
import java.util.UUID;

public interface CustomerService {

    /**
     * Returns all hotels saved by this customer, newest first.
     * Each item includes a nested HotelResponse summary.
     */
    List<SavedHotelResponse> getSavedHotels(UUID customerId);

    /**
     * Saves (favourites) a hotel for the customer.
     * Throws ConflictException if the hotel is already saved.
     */
    SavedHotelResponse saveHotel(UUID customerId, UUID hotelId);

    /**
     * Removes a hotel from the customer's saved list.
     * No-op if the hotel was not saved (idempotent DELETE).
     */
    void unsaveHotel(UUID customerId, UUID hotelId);
}
