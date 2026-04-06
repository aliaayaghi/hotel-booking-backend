package com.HotelBook.HotelBooking.Review.service;

import com.HotelBook.HotelBooking.Review.Entity.Review;
import com.HotelBook.HotelBooking.Review.dto.ReviewRequestDTO;
import com.HotelBook.HotelBooking.Review.dto.ReviewResponseDTO;
import com.HotelBook.HotelBooking.Common.exception.ReviewAlreadyExistsException;
import com.HotelBook.HotelBooking.Common.exception.ReviewNotFoundException;
import com.HotelBook.HotelBooking.Review.mapper.ReviewMapper;
import com.HotelBook.HotelBooking.Review.repository.ReviewRepository;
import com.HotelBook.HotelBooking.Review.specification.ReviewSpecifications;
import com.HotelBook.HotelBooking.Booking.Booking;
import com.HotelBook.HotelBooking.Booking.BookingRepository;
import com.HotelBook.HotelBooking.Hotel.Hotel;
import com.HotelBook.HotelBooking.Hotel.HotelRepository;
import com.HotelBook.HotelBooking.User.entity.Customer;
import com.HotelBook.HotelBooking.User.repository.CustomerRepository;
import com.HotelBook.HotelBooking.Common.pagination.PagedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final HotelRepository hotelRepository;
    private final CustomerRepository customerRepository;
    private final BookingRepository bookingRepository;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id", "createdAt", "calculatedOverallRating", "customerOverallRating"
    );

    public ReviewServiceImpl(ReviewRepository reviewRepository,
                             HotelRepository hotelRepository,
                             CustomerRepository customerRepository,
                             BookingRepository bookingRepository) {
        this.reviewRepository = reviewRepository;
        this.hotelRepository = hotelRepository;
        this.customerRepository = customerRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public ReviewResponseDTO createReview(ReviewRequestDTO request) {
        if (reviewRepository.existsByBookingId(request.getBookingId())) {
            throw new ReviewAlreadyExistsException(request.getBookingId());
        }

        Hotel hotel = hotelRepository.findById(request.getHotelId())
                .orElseThrow(() -> new RuntimeException("Hotel not found: " + request.getHotelId()));

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found: " + request.getCustomerId()));

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found: " + request.getBookingId()));

        Review review = ReviewMapper.toEntity(request, hotel, customer, booking);
        Review saved = reviewRepository.save(review);
        return ReviewMapper.toDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewResponseDTO getReviewById(UUID id) {          // ← FIX: was Long
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException(id));
        return ReviewMapper.toDTO(review);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReviewResponseDTO> listReviews(
            Pageable pageable,
            UUID hotelId,
            UUID customerId,
            Review.TravelType travelType,
            Integer minRating,
            LocalDateTime createdAfter,
            LocalDateTime createdBefore,
            Boolean onlyFlagged
    ) {
        validateSort(pageable);

        Specification<Review> spec = Specification.where(ReviewSpecifications.notHidden());

        if (hotelId != null)    spec = spec.and(ReviewSpecifications.hasHotelId(hotelId));
        if (customerId != null) spec = spec.and(ReviewSpecifications.hasCustomerId(customerId));
        if (travelType != null) spec = spec.and(ReviewSpecifications.hasTravelType(travelType));
        if (minRating != null)  spec = spec.and(ReviewSpecifications.ratingGreaterThanOrEqual(minRating));
        if (createdAfter != null || createdBefore != null)
            spec = spec.and(ReviewSpecifications.createdBetween(createdAfter, createdBefore));
        if (Boolean.TRUE.equals(onlyFlagged))
            spec = spec.and(ReviewSpecifications.onlyFlagged());

        Page<Review> page = reviewRepository.findAll(spec, pageable);
        var content = page.getContent().stream().map(ReviewMapper::toDTO).toList();
        return PagedResponse.from(page, content);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReviewResponseDTO> getReviewsByHotelId(UUID hotelId, Pageable pageable) {
        Page<Review> page = reviewRepository.findByHotelId(hotelId, pageable);
        var content = page.getContent().stream().map(ReviewMapper::toDTO).toList();
        return PagedResponse.from(page, content);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReviewResponseDTO> getReviewsByCustomerId(UUID customerId, Pageable pageable) {
        Page<Review> page = reviewRepository.findByCustomerId(customerId, pageable);
        var content = page.getContent().stream().map(ReviewMapper::toDTO).toList();
        return PagedResponse.from(page, content);
    }

    @Override
    public ReviewResponseDTO addManagerReply(UUID reviewId, String managerReply) {  // ← FIX: was Long
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));
        review.setManagerReply(managerReply);
        review.setRepliedAt(LocalDateTime.now());
        return ReviewMapper.toDTO(reviewRepository.save(review));
    }

    @Override
    public ReviewResponseDTO flagReview(UUID reviewId) {       // ← FIX: was Long
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));
        review.setFlagged(true);
        return ReviewMapper.toDTO(reviewRepository.save(review));
    }

    @Override
    public ReviewResponseDTO hideReview(UUID reviewId) {       // ← FIX: was Long
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException(reviewId));
        review.setHidden(true);
        return ReviewMapper.toDTO(reviewRepository.save(review));
    }

    @Override
    public void deleteReview(UUID id) {                        // ← FIX: was Long
        if (!reviewRepository.existsById(id)) {
            throw new ReviewNotFoundException(id);
        }
        reviewRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Double> getAverageScoresForHotel(UUID hotelId) {
        Map<String, Double> scores = new HashMap<>();
        scores.put("cleanliness", reviewRepository.getAverageCleanlinessScore(hotelId));
        scores.put("location",    reviewRepository.getAverageLocationScore(hotelId));
        scores.put("service",     reviewRepository.getAverageServiceScore(hotelId));
        scores.put("value",       reviewRepository.getAverageValueScore(hotelId));
        scores.put("comfort",     reviewRepository.getAverageComfortScore(hotelId));
        scores.put("overall",     reviewRepository.getAverageRatingForHotel(hotelId));
        return scores;
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageRatingForHotel(UUID hotelId) {
        return reviewRepository.getAverageRatingForHotel(hotelId);
    }

    private void validateSort(Pageable pageable) {
        for (Sort.Order order : pageable.getSort()) {
            if (!ALLOWED_SORT_FIELDS.contains(order.getProperty())) {
                throw new RuntimeException("Invalid sort field: " + order.getProperty());
            }
        }
    }
}