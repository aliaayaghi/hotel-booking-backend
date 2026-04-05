package com.HotelBook.HotelBooking.Payment;

import com.HotelBook.HotelBooking.Common.dto.ApiResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/api/bookings/{bookingId}/payment")
    public ResponseEntity<ApiResponseDTO<PaymentResponseDTO>> processPayment(
            @RequestHeader("X-Customer-Id") UUID customerId,
            @PathVariable UUID bookingId,
            @Valid @RequestBody PaymentRequestDTO request) {

        PaymentResponseDTO payment = paymentService.processPayment(bookingId, customerId, request);

        String message = payment.getStatus().name().equals("PAID")
                ? "Payment successful. Booking is now confirmed and your dates are reserved."
                : "Payment failed. Your booking is still pending — please try again.";

        return ResponseEntity.ok(ApiResponseDTO.success(payment, message));
    }

    @PostMapping("/api/bookings/{bookingId}/payment/refund")
    public ResponseEntity<ApiResponseDTO<PaymentResponseDTO>> processRefund(
            @RequestHeader("X-Customer-Id") UUID customerId,
            @PathVariable UUID bookingId) {

        PaymentResponseDTO payment = paymentService.processRefund(bookingId, customerId);

        String message = payment.getRefundAmount() != null
                && payment.getRefundAmount().compareTo(BigDecimal.ZERO) > 0
                ? "Booking cancelled. Refund of $" + payment.getRefundAmount() + " will be processed within 3–5 business days."
                : "Booking cancelled. No refund applies based on your selected cancellation policy.";

        return ResponseEntity.ok(ApiResponseDTO.success(payment, message));
    }

    @GetMapping("/api/bookings/{bookingId}/payment")
    public ResponseEntity<ApiResponseDTO<PaymentResponseDTO>> getPaymentForBooking(
            @RequestHeader("X-Customer-Id") UUID customerId,
            @PathVariable UUID bookingId) {

        PaymentResponseDTO payment = paymentService.getPaymentByBookingId(bookingId, customerId);
        return ResponseEntity.ok(ApiResponseDTO.success(payment, "Payment retrieved."));
    }

    @GetMapping("/api/payments/{paymentId}")
    public ResponseEntity<ApiResponseDTO<PaymentResponseDTO>> getPaymentById(
            @PathVariable UUID paymentId) {

        PaymentResponseDTO payment = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(ApiResponseDTO.success(payment, "Payment retrieved."));
    }
}