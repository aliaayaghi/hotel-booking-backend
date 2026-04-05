package com.HotelBook.HotelBooking.HotelAccessibility;

import com.HotelBook.HotelBooking.Hotel.Hotel;
import com.HotelBook.HotelBooking.Hotel.HotelRepository;
import com.HotelBook.HotelBooking.Common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotelAccessibilityServiceImpl implements HotelAccessibilityService {

    private final HotelAccessibilityRepository accessibilityRepository;
    private final HotelRepository hotelRepository;

    @Override
    @Transactional(readOnly = true)
    public List<HotelAccessibilityResponse> getAccessibilityFeatures(UUID hotelId) {
        return accessibilityRepository.findByHotelId(hotelId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public HotelAccessibilityResponse addFeature(UUID hotelId, CreateAccessibilityRequest request) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", hotelId));

        HotelAccessibility feature = HotelAccessibility.builder()
                .hotel(hotel)
                .feature(request.getFeature())
                .level(request.getLevel())
                .description(request.getDescription())
                .build();

        feature = accessibilityRepository.save(feature);
        log.info("Added accessibility feature '{}' (level={}) to hotel {}",
                feature.getFeature(), feature.getLevel(), hotelId);
        return toResponse(feature);
    }

    @Override
    @Transactional
    public void removeFeature(UUID hotelId, UUID featureId) {
        HotelAccessibility feature = accessibilityRepository.findById(featureId)
                .orElseThrow(() -> new ResourceNotFoundException("HotelAccessibility", featureId));

        if (!feature.getHotel().getId().equals(hotelId)) {
            throw new ResourceNotFoundException("HotelAccessibility", featureId);
        }

        accessibilityRepository.delete(feature);
        log.info("Removed accessibility feature {} from hotel {}", featureId, hotelId);
    }

    // ── Private mapper ─────────────────────────────────────────────────────────

    private HotelAccessibilityResponse toResponse(HotelAccessibility entity) {
        return HotelAccessibilityResponse.builder()
                .id(entity.getId())
                .feature(entity.getFeature())
                .level(entity.getLevel())
                .description(entity.getDescription())
                .build();
    }
}