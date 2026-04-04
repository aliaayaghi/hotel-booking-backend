package com.HotelBook.HotelBooking.notification;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for notification list views.
 *
 * NOTE: 'body' is intentionally excluded from this DTO to keep list
 * responses lightweight. The full email body is only exposed on
 * GET /api/notifications/{id}.
 *
 * If you want to add the body to this response (e.g. for a notifications
 * panel), add the field and update the mapper accordingly.
 */
@Data
@Builder
public class NotificationResponse {

    private UUID id;
    private UUID recipientId;
    private NotificationType type;
    private NotificationChannel channel;
    private String subject;
    private NotificationStatus status;
    private Instant sentAt;
    private Instant createdAt;

    // body is excluded — use GET /api/notifications/{id} for full body
}