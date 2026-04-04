package com.HotelBook.HotelBooking.notification;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

/**
 * Request DTO for POST /api/notifications/send
 *
 * This endpoint is called internally (M2 calls NotificationService directly as a Spring bean),
 * but the controller endpoint is also exposed so an admin/manager can manually
 * trigger a notification if needed.
 *
 * bookingId and paymentId are optional metadata stored in the Notification.metadata
 * JSON field for traceability.
 */
@Data
public class SendNotificationRequest {

    @NotNull(message = "Recipient ID is required")
    private UUID recipientId;

    @NotNull(message = "Notification type is required")
    private NotificationType type;

    // Optional — context for the email body template
    private UUID bookingId;
    private UUID paymentId;
}
