package com.HotelBook.HotelBooking.Notification;


import com.HotelBook.HotelBooking.User.entity.User;
import com.HotelBook.HotelBooking.User.repository.UserRepository;
import com.HotelBook.HotelBooking.Common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final MockEmailSender emailSender;
    private final UserRepository userRepository;

    // ── Core send ──────────────────────────────────────────────────────────────

    /**
     * Generic send method.
     * 1. Builds the email subject and body from the request type.
     * 2. Creates a Notification record with status=PENDING and saves it.
     * 3. Attempts to send via MockEmailSender.
     * 4. Updates the status to SENT or FAILED, then saves again.
     *
     * The notification row is ALWAYS persisted — even on failure.
     * This gives an audit trail and enables retry later.
     */
    @Override
    @Transactional
    public NotificationResponse send(SendNotificationRequest request) {
        // Fetch recipient to get their email address
        User recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getRecipientId()));

        // Build subject and body from the notification type
        EmailContent content = buildEmailContent(
                request.getType(),
                request.getBookingId(),
                request.getPaymentId()
        );

        // Step 1: Persist as PENDING
        Notification notification = Notification.builder()
                .recipientId(request.getRecipientId())
                .type(request.getType())
                .channel(NotificationChannel.EMAIL)
                .subject(content.subject)
                .body(content.body)
                .status(NotificationStatus.PENDING)
                .metadata(buildMetadata(request.getBookingId(), request.getPaymentId()))
                .build();

        notification = notificationRepository.save(notification);

        // Step 2: Attempt to send
        try {
            emailSender.send(recipient.getEmail(), content.subject, content.body);

            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(Instant.now());

            log.info("Notification SENT: type={} recipient={}", request.getType(), recipient.getEmail());

        } catch (Exception e) {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setErrorMessage(e.getClass().getSimpleName() + ": " + e.getMessage());

            log.error("Notification FAILED: type={} recipient={} error={}",
                    request.getType(), recipient.getEmail(), e.getMessage());
        }

        // Step 3: Save final status (SENT or FAILED)
        notification = notificationRepository.save(notification);

        return toResponse(notification);
    }

    // ── Query methods ──────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getById(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", id));
        return toResponse(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getByRecipient(UUID recipientId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── M2 convenience wrappers ────────────────────────────────────────────────

    /**
     * M2 calls this from BookingService after confirming a booking.
     * Fire-and-forget — never throws even if sending fails.
     */
    @Override
    public void sendBookingConfirmation(UUID bookingId, UUID customerId) {
        try {
            SendNotificationRequest request = new SendNotificationRequest();
            request.setRecipientId(customerId);
            request.setType(NotificationType.BOOKING_CONFIRMED);
            request.setBookingId(bookingId);
            send(request);
        } catch (Exception e) {
            // Never allow notification failure to bubble up into M2's booking flow
            log.error("sendBookingConfirmation failed for bookingId={} customerId={}: {}",
                    bookingId, customerId, e.getMessage());
        }
    }

    /**
     * M2 calls this from BookingService when a booking is cancelled.
     */
    @Override
    public void sendCancellationNotice(UUID bookingId, UUID customerId) {
        try {
            SendNotificationRequest request = new SendNotificationRequest();
            request.setRecipientId(customerId);
            request.setType(NotificationType.BOOKING_CANCELLED);
            request.setBookingId(bookingId);
            send(request);
        } catch (Exception e) {
            log.error("sendCancellationNotice failed for bookingId={} customerId={}: {}",
                    bookingId, customerId, e.getMessage());
        }
    }

    /**
     * M2 calls this from PaymentService after successful payment.
     */
    @Override
    public void sendPaymentReceipt(UUID paymentId, UUID customerId) {
        try {
            SendNotificationRequest request = new SendNotificationRequest();
            request.setRecipientId(customerId);
            request.setType(NotificationType.PAYMENT_RECEIVED);
            request.setPaymentId(paymentId);
            send(request);
        } catch (Exception e) {
            log.error("sendPaymentReceipt failed for paymentId={} customerId={}: {}",
                    paymentId, customerId, e.getMessage());
        }
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    /**
     * Builds the email subject and body template for each notification type.
     * Extend these templates with HTML formatting for Step 2.
     */
    private EmailContent buildEmailContent(NotificationType type, UUID bookingId, UUID paymentId) {
        return switch (type) {
            case BOOKING_CONFIRMED -> new EmailContent(
                    "Your booking is confirmed! 🎉",
                    """
                    Dear Guest,
                    
                    Your booking has been confirmed successfully.
                    Booking Reference: %s
                    
                    We look forward to welcoming you!
                    
                    Best regards,
                    The HotelBook Team
                    """.formatted(bookingId != null ? bookingId.toString() : "N/A")
            );

            case BOOKING_CANCELLED -> new EmailContent(
                    "Your booking has been cancelled",
                    """
                    Dear Guest,
                    
                    Your booking (Reference: %s) has been cancelled.
                    
                    If you did not request this cancellation, please contact our support team.
                    
                    Best regards,
                    The HotelBook Team
                    """.formatted(bookingId != null ? bookingId.toString() : "N/A")
            );

            case PAYMENT_RECEIVED -> new EmailContent(
                    "Payment received — Thank you!",
                    """
                    Dear Guest,
                    
                    We have received your payment successfully.
                    Payment Reference: %s
                    
                    Your booking is now confirmed.
                    
                    Best regards,
                    The HotelBook Team
                    """.formatted(paymentId != null ? paymentId.toString() : "N/A")
            );

            case PAYMENT_FAILED -> new EmailContent(
                    "Payment failed — Action required",
                    """
                    Dear Guest,
                    
                    Unfortunately, your payment could not be processed.
                    Payment Reference: %s
                    
                    Please update your payment details or contact your bank.
                    
                    Best regards,
                    The HotelBook Team
                    """.formatted(paymentId != null ? paymentId.toString() : "N/A")
            );
        };
    }

    /**
     * Build a JSON string for the metadata field.
     * Only includes non-null IDs.
     */
    private String buildMetadata(UUID bookingId, UUID paymentId) {
        StringBuilder sb = new StringBuilder("{");
        boolean hasEntry = false;

        if (bookingId != null) {
            sb.append("\"bookingId\":\"").append(bookingId).append("\"");
            hasEntry = true;
        }
        if (paymentId != null) {
            if (hasEntry) sb.append(",");
            sb.append("\"paymentId\":\"").append(paymentId).append("\"");
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Maps Notification entity to NotificationResponse DTO.
     * Body is intentionally excluded from the list view DTO.
     */
    private NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .recipientId(notification.getRecipientId())
                .type(notification.getType())
                .channel(notification.getChannel())
                .subject(notification.getSubject())
                .status(notification.getStatus())
                .sentAt(notification.getSentAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    /** Simple record to hold the generated email subject + body */
    private record EmailContent(String subject, String body) {}
}
