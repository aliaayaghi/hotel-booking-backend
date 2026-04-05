package com.HotelBook.HotelBooking.Review.controller;

import com.HotelBook.HotelBooking.Review.Entity.Review;
import com.HotelBook.HotelBooking.Review.dto.ReviewRequestDTO;
import com.HotelBook.HotelBooking.Review.dto.ReviewResponseDTO;
import com.HotelBook.HotelBooking.Review.service.ReviewService;
import com.HotelBook.HotelBooking.common.dto.ApiResponseDTO;
import com.HotelBook.HotelBooking.common.pagination.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Operation(summary = "List reviews with optional filters")
    @GetMapping
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

    @Operation(summary = "Get reviews for a specific hotel")
    @GetMapping("/hotel/{hotelId}")
    public ResponseEntity<ApiResponseDTO<PagedResponse<ReviewResponseDTO>>> getReviewsByHotel(
            @PathVariable UUID hotelId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                reviewService.getReviewsByHotelId(hotelId, pageable),
                "Reviews for hotel " + hotelId + " fetched successfully"
        ));
    }

    @Operation(summary = "Get reviews by a specific customer")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<ApiResponseDTO<PagedResponse<ReviewResponseDTO>>> getReviewsByCustomer(
            @PathVariable UUID customerId,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                reviewService.getReviewsByCustomerId(customerId, pageable),
                "Reviews for customer " + customerId + " fetched successfully"
        ));
    }

    @Operation(summary = "Get average scores for a hotel")
    @GetMapping("/hotel/{hotelId}/average-scores")
    public ResponseEntity<ApiResponseDTO<Map<String, Double>>> getAverageScores(
            @PathVariable UUID hotelId
    ) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                reviewService.getAverageScoresForHotel(hotelId),
                "Average scores for hotel " + hotelId + " fetched successfully"
        ));
    }

    @Operation(summary = "Create a new review")
    @PostMapping
    public ResponseEntity<ApiResponseDTO<ReviewResponseDTO>> createReview(
            @Valid @RequestBody ReviewRequestDTO request,
            UriComponentsBuilder uriBuilder
    ) {
        ReviewResponseDTO created = reviewService.createReview(request);

        URI location = uriBuilder
                .path("/api/reviews/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity
                .created(location)
                .body(ApiResponseDTO.success(created, "Review created successfully"));
    }

    @Operation(summary = "Get review by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<ReviewResponseDTO>> getReviewById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                reviewService.getReviewById(id),
                "Review fetched successfully"
        ));
    }

    @Operation(summary = "Manager adds a reply to a review")
    @PatchMapping("/{id}/reply")
    public ResponseEntity<ApiResponseDTO<ReviewResponseDTO>> addManagerReply(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload
    ) {
        String reply = payload.get("reply");
        return ResponseEntity.ok(ApiResponseDTO.success(
                reviewService.addManagerReply(id, reply),
                "Reply added successfully"
        ));
    }

    @Operation(summary = "Flag a review as abusive")
    @PatchMapping("/{id}/flag")
    public ResponseEntity<ApiResponseDTO<ReviewResponseDTO>> flagReview(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                reviewService.flagReview(id),
                "Review flagged successfully"
        ));
    }

    @Operation(summary = "Admin hides a review")
    @PatchMapping("/{id}/hide")
    public ResponseEntity<ApiResponseDTO<ReviewResponseDTO>> hideReview(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(ApiResponseDTO.success(
                reviewService.hideReview(id),
                "Review hidden successfully"
        ));
    }

    @Operation(summary = "Delete a review")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body(ApiResponseDTO.success(null, "Review deleted successfully"));
    }
}