package com.HotelBook.HotelBooking.catalog.customer;


import com.HotelBook.HotelBooking.catalog.user.entity.User;
import com.HotelBook.HotelBooking.notification.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.HotelBook.HotelBooking.savedhotel.SavedHotelResponseDTO;

import java.util.List;

@RestController
@RequestMapping("/api/customers/me")
@PreAuthorize("hasRole('CUSTOMER')")
@RequiredArgsConstructor
@Tag(name = "Customer", description = "Customer-facing endpoints for bookings, reviews, saved hotels, and notifications")
@SecurityRequirement(name = "bearerAuth")
public class CustomerController {

    private final CustomerService customerService;
    private final NotificationService notificationService;

    // ══════════════════════════════════════════════════════════════════════════
    // BOOKINGS — STUB (owned by M2)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/customers/me/bookings
     *
     * Step 1 stub — returns an empty list.
     * M2 will inject their BookingService here once it's ready.
     * The endpoint must exist for Swagger docs and frontend routing.
     *
     * INTEGRATION NOTE FOR M2:
     *   Inject BookingService here and call:
     *   bookingService.getBookingsByCustomerId(currentUser.getId())
     */
    @GetMapping("/bookings")
    @Operation(
            summary = "Get customer bookings",
            description = "Returns all bookings for the authenticated customer. Stub in Step 1 — returns empty list. M2 implements the real data."
    )
    @ApiResponse(responseCode = "200", description = "Bookings returned (empty list in Step 1)")
    public ResponseEntity<List<Object>> getBookings(
            @AuthenticationPrincipal User currentUser
    ) {
        // TODO (M2 integration): inject BookingService and call getBookingsByCustomerId()
        return ResponseEntity.ok(List.of());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // REVIEWS — STUB (owned by M3)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/customers/me/reviews
     *
     * Step 1 stub — returns an empty list.
     * M3 will inject their ReviewService here once it's ready.
     *
     * INTEGRATION NOTE FOR M3:
     *   Inject ReviewService here and call:
     *   reviewService.getReviewsByCustomerId(currentUser.getId())
     */
    @GetMapping("/reviews")
    @Operation(
            summary = "Get customer reviews",
            description = "Returns all reviews written by the authenticated customer. Stub in Step 1 — returns empty list. M3 implements the real data."
    )
    @ApiResponse(responseCode = "200", description = "Reviews returned (empty list in Step 1)")
    public ResponseEntity<List<Object>> getReviews(
            @AuthenticationPrincipal User currentUser
    ) {
        // TODO (M3 integration): inject ReviewService and call getReviewsByCustomerId()
        return ResponseEntity.ok(List.of());
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SAVED HOTELS (WISHLIST)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/customers/me/saved
     * Returns all saved hotels for the authenticated customer, sorted newest first.
     */
    @GetMapping("/saved")
    @Operation(
            summary = "Get saved hotels",
            description = "Returns all hotels the customer has saved/favourited, sorted by date saved (newest first)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Saved hotels returned successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not a customer")
    })
    public ResponseEntity<List<SavedHotelResponse>> getSavedHotels(
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(customerService.getSavedHotels(currentUser.getId()));
    }

    /**
     * POST /api/customers/me/saved/{hotelId}
     * Save (favourite) a hotel. Returns 409 if already saved.
     */
    @PostMapping("/saved/{hotelId}")
    @Operation(
            summary = "Save a hotel",
            description = "Adds a hotel to the customer's saved list. Returns 409 if already saved."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Hotel saved successfully"),
            @ApiResponse(responseCode = "404", description = "Hotel not found"),
            @ApiResponse(responseCode = "409", description = "Hotel already saved"),
            @ApiResponse(responseCode = "403", description = "Not a customer")
    })
    public ResponseEntity<SavedHotelResponse> saveHotel(
            @AuthenticationPrincipal User currentUser,
            @Parameter(description = "Hotel UUID to save", required = true)
            @PathVariable java.util.UUID hotelId
    ) {
        SavedHotelResponse response = customerService.saveHotel(currentUser.getId(), hotelId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * DELETE /api/customers/me/saved/{hotelId}
     * Remove a hotel from saved list. Idempotent — 204 even if not saved.
     */
    @DeleteMapping("/saved/{hotelId}")
    @Operation(
            summary = "Unsave a hotel",
            description = "Removes a hotel from the customer's saved list. Idempotent — returns 204 even if the hotel was not saved."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Hotel removed from saved list"),
            @ApiResponse(responseCode = "403", description = "Not a customer")
    })
    public ResponseEntity<Void> unsaveHotel(
            @AuthenticationPrincipal User currentUser,
            @Parameter(description = "Hotel UUID to unsave", required = true)
            @PathVariable java.util.UUID hotelId
    ) {
        customerService.unsaveHotel(currentUser.getId(), hotelId);
        return ResponseEntity.noContent().build();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // NOTIFICATIONS
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * GET /api/customers/me/notifications
     * Returns all notifications for the authenticated customer (newest first).
     * Delegates to NotificationService which is owned by the notification package.
     */
    @GetMapping("/notifications")
    @Operation(
            summary = "Get customer notifications",
            description = "Returns all notifications sent to the authenticated customer, sorted by date (newest first)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Notifications returned successfully"),
            @ApiResponse(responseCode = "403", description = "Not a customer")
    })
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(notificationService.getByRecipient(currentUser.getId()));
    }
}