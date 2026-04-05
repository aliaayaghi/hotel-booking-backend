package com.HotelBook.HotelBooking.Booking;

import com.HotelBook.HotelBooking.Common.dto.ApiResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<ApiResponseDTO<BookingResponseDTO>> createBooking(
            @RequestHeader("X-Customer-Id") UUID customerId,
            @Valid @RequestBody BookingRequestDTO request) {

        BookingResponseDTO booking = bookingService.createBooking(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(booking, "Booking created successfully. Proceed to payment."));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<BookingResponseDTO>>> getMyBookings(
            @RequestHeader("X-Customer-Id") UUID customerId) {

        List<BookingResponseDTO> bookings = bookingService.getBookingsByCustomer(customerId);
        return ResponseEntity.ok(ApiResponseDTO.success(bookings, "Bookings retrieved"));
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<ApiResponseDTO<BookingResponseDTO>> getBooking(
            @RequestHeader("X-Customer-Id") UUID customerId,
            @PathVariable UUID bookingId) {

        BookingResponseDTO booking = bookingService.getBookingById(bookingId, customerId);
        return ResponseEntity.ok(ApiResponseDTO.success(booking, "Booking retrieved"));
    }

    @PatchMapping("/{bookingId}/cancel")
    public ResponseEntity<ApiResponseDTO<BookingResponseDTO>> cancelBooking(
            @RequestHeader("X-Customer-Id") UUID customerId,
            @PathVariable UUID bookingId) {

        BookingResponseDTO booking = bookingService.cancelBooking(bookingId, customerId, "CUSTOMER");
        return ResponseEntity.ok(ApiResponseDTO.success(booking, "Booking cancelled successfully."));
    }

    @PatchMapping("/{bookingId}/complete")
    public ResponseEntity<ApiResponseDTO<BookingResponseDTO>> completeBooking(
            @PathVariable UUID bookingId) {

        BookingResponseDTO booking = bookingService.completeBooking(bookingId);
        return ResponseEntity.ok(ApiResponseDTO.success(booking, "Booking marked as completed."));
    }

    @PatchMapping("/{bookingId}/no-show")
    public ResponseEntity<ApiResponseDTO<BookingResponseDTO>> markNoShow(
            @PathVariable UUID bookingId) {

        BookingResponseDTO booking = bookingService.markNoShow(bookingId);
        return ResponseEntity.ok(ApiResponseDTO.success(booking, "Booking marked as no-show."));
    }

    @GetMapping("/hotels/{hotelId}")
    public ResponseEntity<ApiResponseDTO<List<BookingResponseDTO>>> getHotelBookings(
            @PathVariable UUID hotelId) {

        List<BookingResponseDTO> bookings = bookingService.getBookingsByHotel(hotelId);
        return ResponseEntity.ok(ApiResponseDTO.success(bookings, "Hotel bookings retrieved"));
    }
}