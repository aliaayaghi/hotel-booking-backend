package com.HotelBook.HotelBooking.Customer;

import com.HotelBook.HotelBooking.Common.dto.ApiResponseDTO;
import com.HotelBook.HotelBooking.Notification.NotificationResponse;
import com.HotelBook.HotelBooking.Notification.NotificationService;
import com.HotelBook.HotelBooking.SavedHotel.SavedHotelRequestDTO;
import com.HotelBook.HotelBooking.SavedHotel.SavedHotelResponseDTO;
import com.HotelBook.HotelBooking.SavedHotel.SavedHotelService;
import com.HotelBook.HotelBooking.User.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers/me")
@PreAuthorize("hasRole('CUSTOMER')")
@RequiredArgsConstructor
@Tag(name = "Customer", description = "Customer self-service: bookings, reviews, wishlist, notifications")
@SecurityRequirement(name = "bearerAuth")
public class CustomerController {

    private final SavedHotelService savedHotelService;
    private final NotificationService notificationService;

    // ── Bookings (stub) ───────────────────────────────────────────────────────

    @GetMapping("/bookings")
    @Operation(summary = "Get my bookings", description = "Step 1 stub — returns empty list.")
    public ResponseEntity<ApiResponseDTO<List<Object>>> getBookings(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponseDTO.success(List.of(), "0 booking(s) retrieved."));
    }

    // ── Reviews (stub) ────────────────────────────────────────────────────────

    @GetMapping("/reviews")
    @Operation(summary = "Get my reviews", description = "Step 1 stub — returns empty list.")
    public ResponseEntity<ApiResponseDTO<List<Object>>> getReviews(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponseDTO.success(List.of(), "0 review(s) retrieved."));
    }

    // ── Saved Hotels ──────────────────────────────────────────────────────────

    @GetMapping("/saved")
    @Operation(summary = "Get my saved hotels")
    public ResponseEntity<ApiResponseDTO<List<SavedHotelResponseDTO>>> getSavedHotels(
            @AuthenticationPrincipal User currentUser) {

        List<SavedHotelResponseDTO> saved = savedHotelService.getSavedHotels(currentUser.getId());
        return ResponseEntity.ok(ApiResponseDTO.success(saved, saved.size() + " saved hotel(s) retrieved."));
    }

    @PostMapping("/saved/{hotelId}")
    @Operation(summary = "Save a hotel")
    public ResponseEntity<ApiResponseDTO<SavedHotelResponseDTO>> saveHotel(
            @AuthenticationPrincipal User currentUser,
            @Parameter(description = "UUID of the hotel to save", required = true)
            @PathVariable UUID hotelId,
            @Valid @RequestBody(required = false) SavedHotelRequestDTO request) {

        SavedHotelResponseDTO saved = savedHotelService.saveHotel(currentUser.getId(), hotelId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(saved, "Hotel saved to wishlist."));
    }

    @DeleteMapping("/saved/{hotelId}")
    @Operation(summary = "Unsave a hotel")
    public ResponseEntity<Void> unsaveHotel(
            @AuthenticationPrincipal User currentUser,
            @Parameter(description = "UUID of the hotel to unsave", required = true)
            @PathVariable UUID hotelId) {

        savedHotelService.unsaveHotel(currentUser.getId(), hotelId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/saved/{hotelId}/status")
    @Operation(summary = "Check if hotel is saved")
    public ResponseEntity<ApiResponseDTO<Map<String, Boolean>>> checkSavedStatus(
            @AuthenticationPrincipal User currentUser,
            @Parameter(description = "UUID of the hotel to check", required = true)
            @PathVariable UUID hotelId) {

        boolean isSaved = savedHotelService.isSaved(currentUser.getId(), hotelId);
        return ResponseEntity.ok(ApiResponseDTO.success(Map.of("saved", isSaved), "Status retrieved."));
    }

    @PatchMapping("/saved/{hotelId}/notes")
    @Operation(summary = "Update notes on a saved hotel")
    public ResponseEntity<ApiResponseDTO<SavedHotelResponseDTO>> updateNotes(
            @AuthenticationPrincipal User currentUser,
            @Parameter(description = "UUID of the saved hotel", required = true)
            @PathVariable UUID hotelId,
            @Valid @RequestBody SavedHotelRequestDTO request) {

        SavedHotelResponseDTO updated = savedHotelService.updateNotes(currentUser.getId(), hotelId, request);
        return ResponseEntity.ok(ApiResponseDTO.success(updated, "Notes updated successfully."));
    }

    // ── Notifications ─────────────────────────────────────────────────────────

    @GetMapping("/notifications")
    @Operation(summary = "Get my notifications")
    public ResponseEntity<ApiResponseDTO<List<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal User currentUser) {

        List<NotificationResponse> notifications = notificationService.getByRecipient(currentUser.getId());
        return ResponseEntity.ok(ApiResponseDTO.success(notifications, notifications.size() + " notification(s) retrieved."));
    }
}