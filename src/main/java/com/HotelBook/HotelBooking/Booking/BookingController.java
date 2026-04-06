package com.HotelBook.HotelBooking.Booking;

import com.HotelBook.HotelBooking.Common.dto.ApiResponseDTO;
import com.HotelBook.HotelBooking.User.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * BookingController — fixed version.
 *
 * CHANGES FROM ORIGINAL:
 * 1. Removed all @RequestHeader("X-Customer-Id") parameters.
 *    Customer identity is now extracted from the JWT via @AuthenticationPrincipal.
 *    This is secure — the raw header was trivially spoofable by any caller.
 *
 * 2. Added PATCH /{bookingId}/confirm endpoint (was missing from the original).
 *    This completes the required PENDING → CONFIRMED flow.
 *    Note: payment auto-confirms internally via bookingService.confirmBooking(),
 *    but this explicit endpoint allows admin/manager overrides and is required
 *    by the spec.
 *
 * 3. Added @PreAuthorize role guards on all endpoints.
 *    - CUSTOMER: create, list own, get own, cancel own, pay
 *    - ADMIN / HOTEL_MANAGER: confirm, complete, no-show, hotel bookings list
 *
 * 4. Added @Operation + @Tag Swagger annotations for OpenAPI completeness.
 */
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Booking lifecycle: create, confirm, cancel, complete")
@SecurityRequirement(name = "bearerAuth")
public class BookingController {

    private final BookingService bookingService;

    // ── CREATE — customer creates a PENDING booking ────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(
            summary = "Create a booking",
            description = "Customer creates a booking. Status starts as PENDING. " +
                    "Proceed to POST /api/payments/bookings/{bookingId} to pay and confirm."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Booking created — proceed to payment"),
            @ApiResponse(responseCode = "400", description = "Validation failed or dates invalid"),
            @ApiResponse(responseCode = "409", description = "Room not available for selected dates"),
            @ApiResponse(responseCode = "403", description = "Not a customer")
    })
    public ResponseEntity<ApiResponseDTO<BookingResponseDTO>> createBooking(
            @AuthenticationPrincipal User currentUser,   // ← extracted from JWT, not a raw header
            @Valid @RequestBody BookingRequestDTO request
    ) {
        BookingResponseDTO booking = bookingService.createBooking(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(booking, "Booking created successfully. Proceed to payment."));
    }

    // ── CONFIRM — PENDING → CONFIRMED (admin / manager explicit override) ──────

    /**
     * FIX: This endpoint was missing from the original controller.
     *
     * The payment flow auto-confirms via bookingService.confirmBooking() when
     * payment succeeds. However, the spec requires an explicit PATCH confirm
     * endpoint so that:
     *   a) Admins can manually confirm bookings (e.g. offline payment)
     *   b) The full flow is testable step-by-step in Postman
     *
     * Only ADMIN or HOTEL_MANAGER may call this directly.
     * Customers confirm implicitly through the payment endpoint.
     */
    @PatchMapping("/{bookingId}/confirm")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOTEL_MANAGER')")
    @Operation(
            summary = "Confirm a booking",
            description = "Moves a PENDING booking to CONFIRMED. Blocks room availability for the dates. " +
                    "Normally triggered automatically after successful payment, " +
                    "but this endpoint allows manual confirmation by admins/managers."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking confirmed"),
            @ApiResponse(responseCode = "404", description = "Booking not found"),
            @ApiResponse(responseCode = "409", description = "Booking is not in PENDING status"),
            @ApiResponse(responseCode = "403", description = "Not admin or hotel manager")
    })
    public ResponseEntity<ApiResponseDTO<Void>> confirmBooking(
            @PathVariable UUID bookingId
    ) {
        bookingService.confirmBooking(bookingId);
        return ResponseEntity.ok(ApiResponseDTO.success(null, "Booking confirmed successfully."));
    }

    // ── GET OWN ────────────────────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get my bookings", description = "Returns all bookings for the authenticated customer.")
    public ResponseEntity<ApiResponseDTO<List<BookingResponseDTO>>> getMyBookings(
            @AuthenticationPrincipal User currentUser
    ) {
        List<BookingResponseDTO> bookings = bookingService.getBookingsByCustomer(currentUser.getId());
        return ResponseEntity.ok(ApiResponseDTO.success(bookings, "Bookings retrieved"));
    }

    @GetMapping("/{bookingId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get a single booking", description = "Returns a specific booking owned by the authenticated customer.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking found"),
            @ApiResponse(responseCode = "404", description = "Booking not found or doesn't belong to this customer")
    })
    public ResponseEntity<ApiResponseDTO<BookingResponseDTO>> getBooking(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID bookingId
    ) {
        BookingResponseDTO booking = bookingService.getBookingById(bookingId, currentUser.getId());
        return ResponseEntity.ok(ApiResponseDTO.success(booking, "Booking retrieved"));
    }

    // ── CANCEL ─────────────────────────────────────────────────────────────────

    @PatchMapping("/{bookingId}/cancel")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(
            summary = "Cancel a booking",
            description = "Customer cancels their own PENDING or CONFIRMED booking. " +
                    "If CONFIRMED, room availability is unblocked. " +
                    "Use POST /api/payments/bookings/{bookingId}/refund to get a refund."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Booking cancelled"),
            @ApiResponse(responseCode = "409", description = "Booking cannot be cancelled in its current status")
    })
    public ResponseEntity<ApiResponseDTO<BookingResponseDTO>> cancelBooking(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID bookingId
    ) {
        BookingResponseDTO booking = bookingService.cancelBooking(bookingId, currentUser.getId(), "CUSTOMER");
        return ResponseEntity.ok(ApiResponseDTO.success(booking, "Booking cancelled successfully."));
    }

    // ── COMPLETE / NO-SHOW — staff operations ──────────────────────────────────

    @PatchMapping("/{bookingId}/complete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOTEL_MANAGER')")
    @Operation(
            summary = "Mark booking as completed",
            description = "Admin or hotel manager marks a CONFIRMED booking as COMPLETED after guest check-out."
    )
    public ResponseEntity<ApiResponseDTO<BookingResponseDTO>> completeBooking(
            @PathVariable UUID bookingId
    ) {
        BookingResponseDTO booking = bookingService.completeBooking(bookingId);
        return ResponseEntity.ok(ApiResponseDTO.success(booking, "Booking marked as completed."));
    }

    @PatchMapping("/{bookingId}/no-show")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOTEL_MANAGER')")
    @Operation(
            summary = "Mark booking as no-show",
            description = "Admin or hotel manager marks a CONFIRMED booking as NO_SHOW when guest doesn't arrive."
    )
    public ResponseEntity<ApiResponseDTO<BookingResponseDTO>> markNoShow(
            @PathVariable UUID bookingId
    ) {
        BookingResponseDTO booking = bookingService.markNoShow(bookingId);
        return ResponseEntity.ok(ApiResponseDTO.success(booking, "Booking marked as no-show."));
    }

    // ── HOTEL BOOKINGS LIST — manager view ────────────────────────────────────

    @GetMapping("/hotels/{hotelId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOTEL_MANAGER')")
    @Operation(
            summary = "Get all bookings for a hotel",
            description = "Admin or hotel manager views all bookings for a specific hotel, sorted by check-in date."
    )
    public ResponseEntity<ApiResponseDTO<List<BookingResponseDTO>>> getHotelBookings(
            @PathVariable UUID hotelId
    ) {
        List<BookingResponseDTO> bookings = bookingService.getBookingsByHotel(hotelId);
        return ResponseEntity.ok(ApiResponseDTO.success(bookings, "Hotel bookings retrieved"));
    }
}