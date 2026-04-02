package com.HotelBook.HotelBooking.roomavailability;



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
    public ResponseEntity<List<RoomAvailabilityService.AvailabilitySummary>> getAvailability(
            @PathVariable UUID roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam int roomQuantity) {

        List<RoomAvailabilityService.AvailabilitySummary> summary =
                availabilityService.getAvailabilitySummary(roomId, from, to, roomQuantity);
        return ResponseEntity.ok(summary);
    }


    @GetMapping("/api/hotels/{hotelId}/rooms/{roomId}/availability")
    public ResponseEntity<List<RoomAvailabilityService.AvailabilitySummary>> getAvailabilityNested(
            @PathVariable UUID hotelId,
            @PathVariable UUID roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam int roomQuantity) {


        return getAvailability(roomId, from, to, roomQuantity);
    }

    @GetMapping("/api/rooms/{roomId}/availability/blocked")
    public ResponseEntity<List<RoomAvailability>> getBlockedDates(
            @PathVariable UUID roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        return ResponseEntity.ok(availabilityService.getBlockedDates(roomId, from, to));
    }


    @PostMapping("/api/rooms/{roomId}/availability/block")
    public ResponseEntity<String> blockDates(
            @PathVariable UUID roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "MANAGER_BLOCK") String reason) {

        RoomAvailability.BlockedReason blockedReason;
        try {
            blockedReason = RoomAvailability.BlockedReason.valueOf(reason.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body("Invalid reason. Must be: MAINTENANCE or MANAGER_BLOCK");
        }

        availabilityService.manualBlock(roomId, from, to, blockedReason);
        return ResponseEntity.ok("Dates blocked from " + from + " to " + to
                + " with reason: " + reason);
    }


    @DeleteMapping("/api/rooms/{roomId}/availability/unblock")
    public ResponseEntity<String> unblockDates(
            @PathVariable UUID roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        availabilityService.manualUnblock(roomId, from, to);
        return ResponseEntity.ok("Dates unblocked from " + from + " to " + to);
    }
}
