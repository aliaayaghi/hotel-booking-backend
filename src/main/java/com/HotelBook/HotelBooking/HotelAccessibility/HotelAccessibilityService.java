package com.HotelBook.HotelBooking.HotelAccessibility;


import java.util.List;
import java.util.UUID;

public interface HotelAccessibilityService {

    List<HotelAccessibilityResponse> getAccessibilityFeatures(UUID hotelId);

    HotelAccessibilityResponse addFeature(UUID hotelId, CreateAccessibilityRequest request);

    void removeFeature(UUID hotelId, UUID featureId);
}
