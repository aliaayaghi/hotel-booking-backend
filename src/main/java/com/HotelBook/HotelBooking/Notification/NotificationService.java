package com.HotelBook.HotelBooking.Notification;

import java.util.List;
import java.util.UUID;

/**
 * NotificationService — public contract for the notification system.
 *
 * ─────────────────────────────────────────────────────────────────────────────
 * M2 INTEGRATION NOTES:
 * ─────────────────────────────────────────────────────────────────────────────
 * M2's BookingService and PaymentService should inject this interface as a
 * Spring bean and call the convenience methods below.
 *
 * Agreed method signatures for M2:
 *   notificationService.sendBookingConfirmation(bookingId, customerId)
 *   notificationService.sendCancellationNotice(bookingId, customerId)
 *   notificationService.sendPaymentReceipt(paymentId, customerId)
 *
 * These are simple fire-and-forget calls — they do NOT throw exceptions
 * even if sending fails (the failure is recorded in the Notification row).
 * ─────────────────────────────────────────────────────────────────────────────
 */
public interface NotificationService {

    /**
     * Generic send — builds the notification from the request type and saves+sends it.
     * Used by the internal POST /api/notifications/send endpoint.
     */
    NotificationResponse send(SendNotificationRequest request);

    /**
     * Fetch a single notification by ID (with body included).
     * Used by GET /api/notifications/{id}
     */
    NotificationResponse getById(UUID id);

    /**
     * Fetch all notifications for a recipient, sorted newest first.
     * Used by GET /api/customers/me/notifications
     */
    List<NotificationResponse> getByRecipient(UUID recipientId);

    // ── Convenience wrappers called by M2 ─────────────────────────────────────

    /**
     * Called by M2's BookingService when a booking is confirmed.
     * Builds a "Your booking is confirmed" email and sends it.
     *
     * @param bookingId  UUID of the booking (used in email body + metadata)
     * @param customerId UUID of the customer (recipient)
     */
    void sendBookingConfirmation(UUID bookingId, UUID customerId);

    /**
     * Called by M2's BookingService when a booking is cancelled.
     * Builds a "Your booking has been cancelled" email.
     */
    void sendCancellationNotice(UUID bookingId, UUID customerId);

    /**
     * Called by M2's PaymentService when payment is successfully processed.
     * Builds a "Payment received" receipt email.
     */
    void sendPaymentReceipt(UUID paymentId, UUID customerId);
}