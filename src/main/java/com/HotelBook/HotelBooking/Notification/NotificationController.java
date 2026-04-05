package com.HotelBook.HotelBooking.Notification;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Internal notification sending and retrieval")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    private final NotificationService notificationService;

    // ── POST /api/notifications/send ───────────────────────────────────────────

    /**
     * Manually trigger a notification.
     * This endpoint is for admins/managers — M2 calls the service bean directly
     * rather than going through HTTP. Useful for manual retries and testing.
     */
    @PostMapping("/api/notifications/send")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOTEL_MANAGER')")
    @Operation(
            summary = "Manually send a notification",
            description = "Triggers a notification to a recipient. Normally M2 calls NotificationService directly; this endpoint is for manual sends, retries, or testing."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification sent (or attempted) — check status in response"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "404", description = "Recipient user not found"),
            @ApiResponse(responseCode = "403", description = "Not an admin or manager")
    })
    public ResponseEntity<NotificationResponse> sendNotification(
            @Valid @RequestBody SendNotificationRequest request
    ) {
        return ResponseEntity.ok(notificationService.send(request));
    }

    // ── GET /api/notifications/{id} ────────────────────────────────────────────

    /**
     * Fetch a single notification by UUID.
     * This endpoint includes the full email body (unlike the list endpoint).
     * Any authenticated user can fetch notifications sent to them.
     * Admins can fetch any notification.
     */
    @GetMapping("/api/notifications/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Get notification by ID",
            description = "Returns a single notification including the full email body. Accessible by the recipient or any admin."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notification returned"),
            @ApiResponse(responseCode = "404", description = "Notification not found"),
            @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    public ResponseEntity<NotificationResponse> getById(
            @Parameter(description = "Notification UUID", required = true)
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(notificationService.getById(id));
    }
}