package com.HotelBook.HotelBooking.notification;

/**
 * The business event that triggered this notification.
 * M2 calls NotificationService passing one of these types.
 */
public enum NotificationType {

    /** Booking status changed to CONFIRMED */
    BOOKING_CONFIRMED,

    /** Booking was cancelled by customer or hotel/admin */
    BOOKING_CANCELLED,

    /** Payment was successfully processed */
    PAYMENT_RECEIVED,

    /** Payment attempt failed */
    PAYMENT_FAILED
}