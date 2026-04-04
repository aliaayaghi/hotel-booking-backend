package com.HotelBook.HotelBooking.notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    /**
     * Customer's notification inbox — newest first.
     * Used by GET /api/customers/me/notifications
     */
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId);

    /**
     * Find notifications by status — useful for a future retry scheduler
     * that re-sends FAILED notifications.
     */
    List<Notification> findByStatus(NotificationStatus status);

    /**
     * Count unread/pending notifications for a customer.
     * Can be used for a notification badge on the frontend.
     */
    long countByRecipientIdAndStatus(UUID recipientId, NotificationStatus status);
}