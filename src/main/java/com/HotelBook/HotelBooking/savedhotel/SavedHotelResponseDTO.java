package com.HotelBook.HotelBooking.savedhotel;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class SavedHotelResponseDTO {

    private UUID id;
    private UUID customerId;
    private UUID hotelId;
    private String notes;
    private Instant savedAt;
}