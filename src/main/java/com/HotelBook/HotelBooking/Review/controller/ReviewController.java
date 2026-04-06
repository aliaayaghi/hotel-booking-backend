package com.HotelBook.HotelBooking.Review.controller;

import com.HotelBook.HotelBooking.Review.Entity.Review;
import com.HotelBook.HotelBooking.Review.dto.ReviewRequestDTO;
import com.HotelBook.HotelBooking.Review.dto.ReviewResponseDTO;
import com.HotelBook.HotelBooking.Review.service.ReviewService;
import com.HotelBook.HotelBooking.Common.dto.ApiResponseDTO;
import com.HotelBook.HotelBooking.Common.pagination.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * ReviewController — fixed version.
 *
 * BUG FIXED: All path variables typed as {@code Long id} replaced with {@code UUID id}.
 * The Review entity uses @GeneratedValue(strategy = GenerationType.UUID) which
 * produces UUID primary keys. Passing a Long in the path would throw a
 * MethodArgumentTypeMismatchException at runtime on every review endpoint.
 *
 * Also fixed in ReviewService and ReviewServiceImpl — the interface methods
 * getReviewById(Long), addManagerReply(Long), flagReview(Long), hideReview(Long),
 * deleteReview(Long) must all be updated to UUID as well (see ReviewService.java fix).
 *
 * ADDITIONAL FIXES:
 * - Added @PreAuthorize role guards
 * - Added @Tag and full @Operation/@ApiResponses annotations
 */
@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Reviews", description = "Hotel review lifecycle: create, reply, flag, hide, delete")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // ── List / filter ──────────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "List reviews with optional filters")
    @ApiResponse(responseCode = "200", description = "Reviews returned")
    public ResponseEntity<ApiResponseDTO<PagedResponse<ReviewResponseDTO>>> listReviews(
            @PageableDefault(size = 10)
            @SortDefault.SortDefaults({
                    @SortDefault(sort = "createdAt", direction = Sort.Direction.DESC)
            })
            Pageable pageable,
            @RequestParam(required = false) UUID hotelId,
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) Review.TravelType travelType,
            @RequestParam(required = false) Integer minRating,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdAfter,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime createdBefore,
            @RequestParam(required = false) Boolean onlyFlagged
    ) {
        PagedResponse<ReviewResponseDTO> data = reviewService.listReviews(
                pageable, hotelId, customerId, travelType,
                minRating, createdAfter, createdBefore, onlyFlagged);
        return ResponseEntity.ok(ApiResponseDTO.success(data, "Reviews fetched successfully"));
    }

    @GetMapping("/hotel/{hotelId}")
    @Operation(summary = "Get reviews for a specific hotel")
    public ResponseEntity<ApiResponseDTO<PagedResponse<ReviewResponseDTO>>> getReviewsByHotel(
            @PathVariable UUID hotelId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                reviewService.getReviewsByHotelId(hotelId, pageable),
                "Reviews for hotel " + hotelId + " fetched successfully"
        ));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get reviews by a specific customer")
    public ResponseEntity<ApiResponseDTO<PagedResponse<ReviewResponseDTO>>> getReviewsByCustomer(
            @PathVariable UUID customerId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                reviewService.getReviewsByCustomerId(customerId, pageable),
                "Reviews for customer " + customerId + " fetched successfully"
        ));
    }

    @GetMapping("/hotel/{hotelId}/average-scores")
    @Operation(summary = "Get average scores for a hotel")
    public ResponseEntity<ApiResponseDTO<Map<String, Double>>> getAverageScores(
            @PathVariable UUID hotelId
    ) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                reviewService.getAverageScoresForHotel(hotelId),
                "Average scores for hotel " + hotelId + " fetched successfully"
        ));
    }

    // ── Create ─────────────────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a new review", description = "Customer submits a review for a completed booking.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Review created"),
            @ApiResponse(responseCode = "409", description = "Review already exists for this booking")
    })
    public ResponseEntity<ApiResponseDTO<ReviewResponseDTO>> createReview(
            @Valid @RequestBody ReviewRequestDTO request,
            UriComponentsBuilder uriBuilder
    ) {
        ReviewResponseDTO created = reviewService.createReview(request);
        URI location = uriBuilder
                .path("/api/reviews/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location)
                .body(ApiResponseDTO.success(created, "Review created successfully"));
    }

    // ── Get by ID ──────────────────────────────────────────────────────────────

    /**
     * FIX: was @PathVariable Long id — changed to UUID to match Review entity PK.
     * The original caused MethodArgumentTypeMismatchException on every request.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get review by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Review found"),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    public ResponseEntity<ApiResponseDTO<ReviewResponseDTO>> getReviewById(
            @PathVariable UUID id    // ← FIX: was Long
    ) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                reviewService.getReviewById(id),
                "Review fetched successfully"
        ));
    }

    // ── Manager reply ──────────────────────────────────────────────────────────

    /**
     * FIX: was @PathVariable Long id — changed to UUID.
     */
    @PatchMapping("/{id}/reply")
    @PreAuthorize("hasRole('HOTEL_MANAGER') or hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Manager adds a reply to a review")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reply added"),
            @ApiResponse(responseCode = "403", description = "Not a manager or admin"),
            @ApiResponse(responseCode = "404", description = "Review not found")
    })
    public ResponseEntity<ApiResponseDTO<ReviewResponseDTO>> addManagerReply(
            @PathVariable UUID id,   // ← FIX: was Long
            @RequestBody Map<String, String> payload
    ) {
        String reply = payload.get("reply");
        return ResponseEntity.ok(ApiResponseDTO.success(
                reviewService.addManagerReply(id, reply),
                "Reply added successfully"
        ));
    }

    // ── Flag ───────────────────────────────────────────────────────────────────

    /**
     * FIX: was @PathVariable Long id — changed to UUID.
     */
    @PatchMapping("/{id}/flag")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Flag a review as abusive")
    @ApiResponse(responseCode = "200", description = "Review flagged")
    public ResponseEntity<ApiResponseDTO<ReviewResponseDTO>> flagReview(
            @PathVariable UUID id    // ← FIX: was Long
    ) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                reviewService.flagReview(id),
                "Review flagged successfully"
        ));
    }

    // ── Hide ───────────────────────────────────────────────────────────────────

    /**
     * FIX: was @PathVariable Long id — changed to UUID.
     */
    @PatchMapping("/{id}/hide")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Admin hides a review")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Review hidden"),
            @ApiResponse(responseCode = "403", description = "Not admin")
    })
    public ResponseEntity<ApiResponseDTO<ReviewResponseDTO>> hideReview(
            @PathVariable UUID id    // ← FIX: was Long
    ) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                reviewService.hideReview(id),
                "Review hidden successfully"
        ));
    }

    // ── Delete ─────────────────────────────────────────────────────────────────

    /**
     * FIX: was @PathVariable Long id — changed to UUID.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('CUSTOMER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete a review", description = "Admin or the owning customer can delete a review.")
    @ApiResponse(responseCode = "204", description = "Review deleted")
    public ResponseEntity<ApiResponseDTO<Void>> deleteReview(
            @PathVariable UUID id    // ← FIX: was Long
    ) {
        reviewService.deleteReview(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .body(ApiResponseDTO.success(null, "Review deleted successfully"));
    }
}