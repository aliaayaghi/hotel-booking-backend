package com.HotelBook.HotelBooking.SavedHotel;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SavedHotelRequestDTO {

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;   // optional — null is fine
}