package com.HotelBook.HotelBooking.catalog.customer;


import com.HotelBook.HotelBooking.catalog.hotel.Hotel;
import com.HotelBook.HotelBooking.catalog.hotel.HotelMapper;
import com.HotelBook.HotelBooking.catalog.hotel.HotelRepository;
import com.HotelBook.HotelBooking.catalog.policy.ConflictException;
import com.HotelBook.HotelBooking.catalog.user.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.HotelBook.HotelBooking.savedhotel.SavedHotel;
import com.HotelBook.HotelBooking.savedhotel.SavedHotelRepository;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final SavedHotelRepository savedHotelRepository;
    private final HotelRepository hotelRepository;
    private final HotelMapper hotelMapper;

    // ── Get saved hotels ───────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<SavedHotelResponse> getSavedHotels(UUID customerId) {
        List<SavedHotel> saved = savedHotelRepository.findByCustomerIdOrderBySavedAtDesc(customerId);

        return saved.stream()
                .map(savedHotel -> {
                    Hotel hotel = hotelRepository.findById(savedHotel.getHotelId())
                            .orElse(null);   // hotel may have been deleted — skip gracefully
                    if (hotel == null) return null;

                    return SavedHotelResponse.builder()
                            .savedId(savedHotel.getId())
                            .hotelId(savedHotel.getHotelId())
                            .hotel(hotelMapper.toHotelResponse(hotel))
                            .savedAt(savedHotel.getSavedAt())
                            .build();
                })
                .filter(r -> r != null)   // exclude records where the hotel was deleted
                .collect(Collectors.toList());
    }

    // ── Save hotel ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public SavedHotelResponse saveHotel(UUID customerId, UUID hotelId) {
        // 1. Verify the hotel exists and is active
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", hotelId));

        // 2. Prevent duplicate saves — the DB unique constraint is the ultimate guard,
        //    but we do a pre-check here to return a friendly ConflictException
        //    rather than a generic DataIntegrityViolationException.
        if (savedHotelRepository.existsByCustomerIdAndHotelId(customerId, hotelId)) {
            throw new ConflictException(
                    "Hotel is already saved by this customer"
            );
        }

        // 3. Create and save the record
        SavedHotel saved;
        try {
            saved = savedHotelRepository.save(
                    SavedHotel.builder()
                            .customerId(customerId)
                            .hotelId(hotelId)
                            .build()
            );
        } catch (DataIntegrityViolationException e) {
            // Race condition guard — another request saved the same hotel concurrently
            throw new ConflictException(
                    "Hotel is already saved by this customer"
            );
        }

        log.info("Customer {} saved hotel {}", customerId, hotelId);

        return SavedHotelResponse.builder()
                .savedId(saved.getId())
                .hotelId(saved.getHotelId())
                .hotel(hotelMapper.toHotelResponse(hotel))
                .savedAt(saved.getSavedAt())
                .build();
    }

    // ── Unsave hotel ───────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void unsaveHotel(UUID customerId, UUID hotelId) {
        // Idempotent — deleting a hotel that isn't saved is not an error
        savedHotelRepository.deleteByCustomerIdAndHotelId(customerId, hotelId);
        log.info("Customer {} unsaved hotel {}", customerId, hotelId);
    }
}
