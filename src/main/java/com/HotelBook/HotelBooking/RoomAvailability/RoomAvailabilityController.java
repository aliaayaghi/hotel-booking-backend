package com.HotelBook.HotelBooking.RoomAvailability;

import com.HotelBook.HotelBooking.Common.dto.ApiResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class RoomAvailabilityController {

    private final RoomAvailabilityService availabilityService;

    @GetMapping("/api/rooms/{roomId}/availability")
    public ResponseEntity<ApiResponseDTO<List<AvailabilitySummaryResponseDTO>>> getAvailability(
            @PathVariable UUID roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam int roomQuantity) {

        if (!to.isAfter(from)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDTO.error("'to' must be after 'from'."));
        }

        List<AvailabilitySummaryResponseDTO> summary =
                availabilityService.getAvailabilitySummary(roomId, from, to, roomQuantity);

        return ResponseEntity.ok(ApiResponseDTO.success(summary,
                "Availability retrieved for " + summary.size() + " dates."));
    }

    @GetMapping("/api/hotels/{hotelId}/rooms/{roomId}/availability")
    public ResponseEntity<ApiResponseDTO<List<AvailabilitySummaryResponseDTO>>> getAvailabilityNested(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam int roomQuantity) {

        return getAvailability(roomId, from, to, roomQuantity);
    }

    @GetMapping("/api/rooms/{roomId}/availability/blocked")
    public ResponseEntity<ApiResponseDTO<List<RoomAvailabilityResponseDTO>>> getBlockedDates(
            @PathVariable UUID roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        if (!to.isAfter(from)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDTO.error("'to' must be after 'from'."));
        }

        List<RoomAvailabilityResponseDTO> blocked =
                availabilityService.getBlockedDates(roomId, from, to);

        return ResponseEntity.ok(ApiResponseDTO.success(blocked,
                blocked.size() + " blocked date(s) found."));
    }

    @PostMapping("/api/rooms/{roomId}/availability/block")
    public ResponseEntity<ApiResponseDTO<List<AvailabilitySummaryResponseDTO>>> blockDates(
            @PathVariable UUID roomId,
            @RequestParam int roomQuantity,
            @Valid @RequestBody RoomAvailabilityRequestDTO request) {

        List<AvailabilitySummaryResponseDTO> result =
                availabilityService.manualBlock(roomId, roomQuantity, request);

        return ResponseEntity.ok(ApiResponseDTO.success(result,
                "Dates blocked from " + request.getFromDate() + " to " + request.getToDate()
                        + " (reason: " + request.getReason() + ")."));
    }

    @DeleteMapping("/api/rooms/{roomId}/availability/unblock")
    public ResponseEntity<ApiResponseDTO<List<AvailabilitySummaryResponseDTO>>> unblockDates(
            @PathVariable UUID roomId,
            @RequestParam int roomQuantity,
            @Valid @RequestBody RoomAvailabilityRequestDTO request) {

        List<AvailabilitySummaryResponseDTO> result =
                availabilityService.manualUnblock(roomId, roomQuantity, request);

        return ResponseEntity.ok(ApiResponseDTO.success(result,
                "Dates unblocked from " + request.getFromDate() + " to " + request.getToDate() + "."));
    }
}