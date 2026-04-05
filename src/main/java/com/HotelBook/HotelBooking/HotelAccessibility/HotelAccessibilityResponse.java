package com.HotelBook.HotelBooking.HotelAccessibility;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class HotelAccessibilityResponse {

    private UUID id;
    private String feature;
    private AccessibilityLevel level;
    private String description;
}
