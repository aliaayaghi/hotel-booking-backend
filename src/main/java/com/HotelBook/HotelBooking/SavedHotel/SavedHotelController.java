package com.HotelBook.HotelBooking.SavedHotel;

import com.HotelBook.HotelBooking.Common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/saved-hotels")
@RequiredArgsConstructor
@Tag(name = "Saved Hotels", description = "Customer wishlist — save, unsave, notes")
@SecurityRequirement(name = "bearerAuth")
public class SavedHotelController {

    private final SavedHotelService savedHotelService;

    @GetMapping
    @Operation(summary = "List saved hotels", description = "All hotels saved by this customer, newest first.")
    public ResponseEntity<ApiResponse<List<SavedHotelResponseDTO>>> getSavedHotels(
            @RequestHeader("X-Customer-Id") UUID customerId) {

        List<SavedHotelResponseDTO> list = savedHotelService.getSavedHotels(customerId);
        return ResponseEntity.ok(ApiResponse.success(list.size() + " saved hotel(s) retrieved.", list));
    }

    @PostMapping("/{hotelId}")
    @Operation(summary = "Save a hotel", description = "Adds a hotel to the wishlist. Returns 409 if already saved.")
    public ResponseEntity<ApiResponse<SavedHotelResponseDTO>> saveHotel(
            @RequestHeader("X-Customer-Id") UUID customerId,
            @PathVariable UUID hotelId,
            @Valid @RequestBody(required = false) SavedHotelRequestDTO request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Hotel saved to wishlist.",
                        savedHotelService.saveHotel(customerId, hotelId, request)));
    }

    @DeleteMapping("/{hotelId}")
    @Operation(summary = "Unsave a hotel", description = "Removes a hotel from the wishlist. Idempotent.")
    public ResponseEntity<Void> unsaveHotel(
            @RequestHeader("X-Customer-Id") UUID customerId,
            @PathVariable UUID hotelId) {

        savedHotelService.unsaveHotel(customerId, hotelId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{hotelId}/status")
    @Operation(summary = "Check saved status", description = "Returns { saved: true/false } for this hotel.")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkSavedStatus(
            @RequestHeader("X-Customer-Id") UUID customerId,
            @PathVariable UUID hotelId) {

        return ResponseEntity.ok(ApiResponse.success("Status retrieved.",
                Map.of("saved", savedHotelService.isSaved(customerId, hotelId))));
    }

    @PatchMapping("/{hotelId}/notes")
    @Operation(summary = "Update notes", description = "Update the personal note on a saved hotel.")
    public ResponseEntity<ApiResponse<SavedHotelResponseDTO>> updateNotes(
            @RequestHeader("X-Customer-Id") UUID customerId,
            @PathVariable UUID hotelId,
            @Valid @RequestBody SavedHotelRequestDTO request) {

        return ResponseEntity.ok(ApiResponse.success("Notes updated.",
                savedHotelService.updateNotes(customerId, hotelId, request)));
    }
}