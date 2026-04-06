package com.HotelBook.HotelBooking.Review.service;

import com.HotelBook.HotelBooking.Review.Entity.Review;
import com.HotelBook.HotelBooking.Review.dto.ReviewRequestDTO;
import com.HotelBook.HotelBooking.Review.dto.ReviewResponseDTO;
import com.HotelBook.HotelBooking.Common.pagination.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * ReviewService — fixed version.
 * BUG FIXED: All method parameters typed as {@code Long} changed to {@code UUID}
 * to match the Review entity's primary key type (@GeneratedValue UUID).
 */
public interface ReviewService {

    ReviewResponseDTO createReview(ReviewRequestDTO request);

    ReviewResponseDTO getReviewById(UUID id);          // ← FIX: was Long

    PagedResponse<ReviewResponseDTO> listReviews(
            Pageable pageable,
            UUID hotelId,
            UUID customerId,
            Review.TravelType travelType,
            Integer minRating,
            LocalDateTime createdAfter,
            LocalDateTime createdBefore,
            Boolean onlyFlagged
    );

    PagedResponse<ReviewResponseDTO> getReviewsByHotelId(UUID hotelId, Pageable pageable);

    PagedResponse<ReviewResponseDTO> getReviewsByCustomerId(UUID customerId, Pageable pageable);

    ReviewResponseDTO addManagerReply(UUID reviewId, String managerReply);  // ← FIX: was Long

    ReviewResponseDTO flagReview(UUID reviewId);       // ← FIX: was Long

    ReviewResponseDTO hideReview(UUID reviewId);       // ← FIX: was Long

    void deleteReview(UUID id);                        // ← FIX: was Long

    Map<String, Double> getAverageScoresForHotel(UUID hotelId);

    Double getAverageRatingForHotel(UUID hotelId);
}