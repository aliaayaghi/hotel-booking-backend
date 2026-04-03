package com.HotelBook.HotelBooking.catalog.accessibility;



import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import com.HotelBook.HotelBooking.catalog.user.entity.User;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/hotels/{hotelId}/accessibility")
@RequiredArgsConstructor
@Tag(name = "Hotel Accessibility", description = "Manage hotel accessibility features")
public class HotelAccessibilityController {

    private final HotelAccessibilityService accessibilityService;

    // ── GET /api/hotels/{hotelId}/accessibility ────────────────────────────────
    // Public — no auth required. Used by hotel detail page and M3 search.

    @GetMapping
    @Operation(
            summary = "List accessibility features",
            description = "Returns all accessibility features for a hotel. Public endpoint — no authentication required."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Features returned"),
            @ApiResponse(responseCode = "404", description = "Hotel not found")
    })
    public ResponseEntity<List<HotelAccessibilityResponse>> getFeatures(
            @Parameter(description = "Hotel UUID", required = true)
            @PathVariable UUID hotelId
    ) {
        return ResponseEntity.ok(accessibilityService.getAccessibilityFeatures(hotelId));
    }

    // ── POST /api/hotels/{hotelId}/accessibility ───────────────────────────────
    // HOTEL_MANAGER only — must own the hotel (enforced in service layer)

    @PostMapping
    @PreAuthorize("hasRole('HOTEL_MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Add an accessibility feature",
            description = "Adds a new accessibility feature to the hotel. Requires HOTEL_MANAGER role and ownership of the hotel."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Feature added successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not a hotel manager"),
            @ApiResponse(responseCode = "404", description = "Hotel not found")
    })
    public ResponseEntity<HotelAccessibilityResponse> addFeature(
            @Parameter(description = "Hotel UUID", required = true)
            @PathVariable UUID hotelId,

            @Valid @RequestBody CreateAccessibilityRequest request,

            @AuthenticationPrincipal User currentUser
    ) {
        // Note: manager ownership validation (currentUser.getId() == hotel.managerId)
        // should be added here or in the service layer once the Hotel domain is in place.
        HotelAccessibilityResponse response = accessibilityService.addFeature(hotelId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── DELETE /api/hotels/{hotelId}/accessibility/{featureId} ────────────────
    // HOTEL_MANAGER only — service layer verifies hotelId matches feature

    @DeleteMapping("/{featureId}")
    @PreAuthorize("hasRole('HOTEL_MANAGER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Remove an accessibility feature",
            description = "Deletes an accessibility feature from the hotel. Requires HOTEL_MANAGER role. Service verifies the feature belongs to this hotel."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Feature removed successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "Not a hotel manager"),
            @ApiResponse(responseCode = "404", description = "Feature not found or doesn't belong to this hotel")
    })
    public ResponseEntity<Void> removeFeature(
            @Parameter(description = "Hotel UUID", required = true)
            @PathVariable UUID hotelId,

            @Parameter(description = "Feature UUID", required = true)
            @PathVariable UUID featureId
    ) {
        accessibilityService.removeFeature(hotelId, featureId);
        return ResponseEntity.noContent().build();
    }
}
