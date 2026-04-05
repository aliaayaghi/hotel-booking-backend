package com.HotelBook.HotelBooking.HotelAmenity;

import com.HotelBook.HotelBooking.Hotel.Hotel;
import com.HotelBook.HotelBooking.Hotel.HotelRepository;
import com.HotelBook.HotelBooking.Common.exception.HotelNotFoundException;
import com.HotelBook.HotelBooking.Common.exception.UnauthorizedHotelAccessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotelAmenityServiceImpl implements HotelAmenityService {

    private final HotelAmenityRepository amenityRepository;
    private final HotelRepository hotelRepository;
    private final HotelAmenityMapper amenityMapper;

    @Override
    @Transactional(readOnly = true)
    public List<HotelAmenityResponse> getAmenities(UUID hotelId) {
        verifyHotelExists(hotelId);
        return amenityMapper.toResponseList(amenityRepository.findByHotelId(hotelId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HotelAmenityResponse> getAmenitiesByCategory(UUID hotelId, AmenityCategory category) {
        verifyHotelExists(hotelId);
        return amenityMapper.toResponseList(amenityRepository.findByHotelIdAndCategory(hotelId, category));
    }

    @Override
    @Transactional
    public HotelAmenityResponse addAmenity(UUID hotelId, UUID managerId, CreateAmenityRequest request) {
        Hotel hotel = getHotelOrThrow(hotelId);
        validateManagerOwnership(managerId, hotelId);

        if (amenityRepository.existsByHotelIdAndNameIgnoreCase(hotelId, request.getName())) {
            throw new IllegalArgumentException(
                    "Amenity '" + request.getName() + "' already exists for this hotel"
            );
        }

        HotelAmenity amenity = HotelAmenity.builder()
                .hotel(hotel)
                .name(request.getName())
                .category(request.getCategory())
                .icon(request.getIcon())
                .build();

        amenity = amenityRepository.save(amenity);
        log.info("Added amenity '{}' ({}) to hotel {}", amenity.getName(), amenity.getCategory(), hotelId);
        return amenityMapper.toResponse(amenity);
    }

    @Override
    @Transactional
    public void removeAmenity(UUID hotelId, UUID amenityId, UUID managerId) {
        HotelAmenity amenity = amenityRepository.findById(amenityId)
                .orElseThrow(() -> new HotelNotFoundException(amenityId));

        if (!amenity.getHotel().getId().equals(hotelId)) {
            throw new HotelNotFoundException(amenityId);
        }

        validateManagerOwnership(managerId, hotelId);
        amenityRepository.delete(amenity);
        log.info("Removed amenity '{}' from hotel {}", amenity.getName(), hotelId);
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private Hotel getHotelOrThrow(UUID hotelId) {
        return hotelRepository.findById(hotelId)
                .orElseThrow(() -> new HotelNotFoundException(hotelId));
    }

    private void verifyHotelExists(UUID hotelId) {
        if (!hotelRepository.existsById(hotelId)) {
            throw new HotelNotFoundException(hotelId);
        }
    }

    private void validateManagerOwnership(UUID managerId, UUID hotelId) {
        if (!hotelRepository.existsByIdAndManager_Id(hotelId, managerId)) {
            throw new UnauthorizedHotelAccessException(hotelId);
        }
    }
}