package com.HotelBook.HotelBooking.notification;


/**
 * Lifecycle state of a notification record.
 *
 * Flow: PENDING → SENT (success)
 *              → FAILED (sending error — errorMessage field populated)
 *
 * The Notification row is ALWAYS saved even on failure.
 * This gives us a complete audit trail and enables future retry logic.
 */
public enum NotificationStatus {

    /** Created, not yet sent to the email provider */
    PENDING,

    /** Successfully dispatched to the email provider */
    SENT,

    /** Sending failed — see Notification.errorMessage for the reason */
    FAILED
}