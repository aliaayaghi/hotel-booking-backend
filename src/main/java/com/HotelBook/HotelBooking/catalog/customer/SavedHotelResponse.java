package com.HotelBook.HotelBooking.catalog.customer;

import com.HotelBook.HotelBooking.catalog.hotel.HotelResponse;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class SavedHotelResponse {

    private UUID savedId;          // the SavedHotel record's own UUID
    private UUID hotelId;
    private HotelResponse hotel;   // nested hotel summary (no photos/amenities — use GET /hotels/{id} for detail)
    private Instant savedAt;
}
