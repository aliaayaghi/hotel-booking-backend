package com.HotelBook.HotelBooking.catalog.accessibility;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateAccessibilityRequest {

    @NotBlank(message = "Feature name is required")
    @Size(min = 2, max = 100, message = "Feature name must be between 2 and 100 characters")
    private String feature;

    @NotNull(message = "Accessibility level is required")
    private AccessibilityLevel level;

    // Optional — e.g. "Ramp available at side entrance only"
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}
