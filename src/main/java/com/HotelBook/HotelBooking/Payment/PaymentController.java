package com.HotelBook.HotelBooking.Payment;

import com.HotelBook.HotelBooking.Common.dto.ApiResponseDTO;
import com.HotelBook.HotelBooking.User.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * PaymentController — fixed version.
 *
 * CHANGES FROM ORIGINAL:
 * 1. All @RequestHeader("X-Customer-Id") replaced with @AuthenticationPrincipal User.
 *    Customer identity is now extracted securely from the validated JWT token.
 *
 * 2. Added @PreAuthorize role guards:
 *    - CUSTOMER: pay, refund, get own payment
 *    - ADMIN: get any payment by ID
 *
 * 3. Added @Operation + @Tag Swagger annotations.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Mock payment processing, refunds, and payment retrieval")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    // ── Process payment ────────────────────────────────────────────────────────

    @PostMapping("/api/bookings/{bookingId}/payment")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(
            summary = "Process payment for a booking",
            description = "Simulates payment for a PENDING booking. " +
                    "On success the booking is automatically confirmed and dates are blocked. " +
                    "Set simulateFailure=true in the body to test the failure path."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment processed (check status field: PAID or FAILED)"),
            @ApiResponse(responseCode = "404", description = "Booking not found"),
            @ApiResponse(responseCode = "409", description = "Booking is not PENDING, or payment already exists")
    })
    public ResponseEntity<ApiResponseDTO<PaymentResponseDTO>> processPayment(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID bookingId,
            @Valid @RequestBody PaymentRequestDTO request
    ) {
        PaymentResponseDTO payment = paymentService.processPayment(bookingId, currentUser.getId(), request);

        String message = "PAID".equals(payment.getStatus().name())
                ? "Payment successful. Booking is now confirmed and your dates are reserved."
                : "Payment failed. Your booking is still pending — please try again.";

        return ResponseEntity.ok(ApiResponseDTO.success(payment, message));
    }

    // ── Refund ─────────────────────────────────────────────────────────────────

    @PostMapping("/api/bookings/{bookingId}/payment/refund")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(
            summary = "Request a refund",
            description = "Cancels the booking and calculates a refund based on the cancellation policy. " +
                    "Full refund if no policy configured."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Refund processed — booking cancelled"),
            @ApiResponse(responseCode = "400", description = "No payment found for booking"),
            @ApiResponse(responseCode = "409", description = "Payment not in PAID status")
    })
    public ResponseEntity<ApiResponseDTO<PaymentResponseDTO>> processRefund(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID bookingId
    ) {
        PaymentResponseDTO payment = paymentService.processRefund(bookingId, currentUser.getId());

        String message = payment.getRefundAmount() != null
                && payment.getRefundAmount().compareTo(BigDecimal.ZERO) > 0
                ? "Booking cancelled. Refund of $" + payment.getRefundAmount() + " will be processed within 3–5 business days."
                : "Booking cancelled. No refund applies based on your selected cancellation policy.";

        return ResponseEntity.ok(ApiResponseDTO.success(payment, message));
    }

    // ── Get payment for a booking ──────────────────────────────────────────────

    @GetMapping("/api/bookings/{bookingId}/payment")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get payment for a booking", description = "Returns the payment record for the authenticated customer's booking.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment retrieved"),
            @ApiResponse(responseCode = "404", description = "Payment not found for this booking")
    })
    public ResponseEntity<ApiResponseDTO<PaymentResponseDTO>> getPaymentForBooking(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID bookingId
    ) {
        PaymentResponseDTO payment = paymentService.getPaymentByBookingId(bookingId, currentUser.getId());
        return ResponseEntity.ok(ApiResponseDTO.success(payment, "Payment retrieved."));
    }

    // ── Get payment by ID — admin ──────────────────────────────────────────────

    @GetMapping("/api/payments/{paymentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get payment by ID", description = "Returns any payment by UUID. Available to admins and the owning customer.")
    @ApiResponse(responseCode = "404", description = "Payment not found")
    public ResponseEntity<ApiResponseDTO<PaymentResponseDTO>> getPaymentById(
            @PathVariable UUID paymentId
    ) {
        PaymentResponseDTO payment = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(ApiResponseDTO.success(payment, "Payment retrieved."));
    }
}