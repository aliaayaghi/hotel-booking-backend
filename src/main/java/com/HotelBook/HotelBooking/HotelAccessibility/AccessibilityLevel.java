package com.HotelBook.HotelBooking.HotelAccessibility;

/**
 * Describes how well a hotel supports a particular accessibility feature.
 * Used by M3's search filter — e.g. ?wheelchairAccessible=FULL
 */
public enum AccessibilityLevel {

    /** Feature is fully available and operational */
    FULL,

    /** Feature is partially available — details in the description field */
    PARTIAL,

    /** Feature is not available at this hotel */
    NONE
}
