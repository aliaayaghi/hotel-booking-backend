package com.HotelBook.HotelBooking.SavedHotel;

import com.HotelBook.HotelBooking.Common.dto.ApiResponseDTO;
import com.HotelBook.HotelBooking.User.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    @Operation(summary = "List saved hotels")
    public ResponseEntity<ApiResponseDTO<List<SavedHotelResponseDTO>>> getSavedHotels(
            @AuthenticationPrincipal User currentUser) {

        UUID customerId = currentUser.getId();
        List<SavedHotelResponseDTO> list = savedHotelService.getSavedHotels(customerId);
        return ResponseEntity.ok(ApiResponseDTO.success(list, list.size() + " saved hotel(s) retrieved."));
    }

    @PostMapping("/{hotelId}")
    @Operation(summary = "Save a hotel")
    public ResponseEntity<ApiResponseDTO<SavedHotelResponseDTO>> saveHotel(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID hotelId,
            @Valid @RequestBody(required = false) SavedHotelRequestDTO request) {

        UUID customerId = currentUser.getId();
        SavedHotelResponseDTO saved = savedHotelService.saveHotel(customerId, hotelId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(saved, "Hotel saved to wishlist."));
    }

    @DeleteMapping("/{hotelId}")
    @Operation(summary = "Unsave a hotel")
    public ResponseEntity<Void> unsaveHotel(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID hotelId) {

        UUID customerId = currentUser.getId();
        savedHotelService.unsaveHotel(customerId, hotelId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{hotelId}/status")
    @Operation(summary = "Check saved status")
    public ResponseEntity<ApiResponseDTO<Map<String, Boolean>>> checkSavedStatus(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID hotelId) {

        UUID customerId = currentUser.getId();
        return ResponseEntity.ok(ApiResponseDTO.success(
                Map.of("saved", savedHotelService.isSaved(customerId, hotelId)), "Status retrieved."));
    }

    @PatchMapping("/{hotelId}/notes")
    @Operation(summary = "Update notes")
    public ResponseEntity<ApiResponseDTO<SavedHotelResponseDTO>> updateNotes(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID hotelId,
            @Valid @RequestBody SavedHotelRequestDTO request) {

        UUID customerId = currentUser.getId();
        return ResponseEntity.ok(ApiResponseDTO.success(
                savedHotelService.updateNotes(customerId, hotelId, request), "Notes updated."));
    }
}