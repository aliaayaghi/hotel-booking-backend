package com.HotelBook.HotelBooking.catalog.accessibility;


import com.HotelBook.HotelBooking.catalog.hotel.HotelRepository;
import com.HotelBook.HotelBooking.catalog.user.exception.ResourceNotFoundException;
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
        // Verify the hotel exists before attaching a feature to it
        if (!hotelRepository.existsById(hotelId)) {
            throw new ResourceNotFoundException("Hotel", hotelId);
        }

        HotelAccessibility feature = HotelAccessibility.builder()
                .hotelId(hotelId)
                .feature(request.getFeature())
                .level(request.getLevel())
                .description(request.getDescription())
                .build();

        feature = accessibilityRepository.save(feature);
        log.info("Added accessibility feature '{}' (level={}) to hotel {}", feature.getFeature(), feature.getLevel(), hotelId);
        return toResponse(feature);
    }

    @Override
    @Transactional
    public void removeFeature(UUID hotelId, UUID featureId) {
        HotelAccessibility feature = accessibilityRepository.findById(featureId)
                .orElseThrow(() -> new ResourceNotFoundException("HotelAccessibility", featureId));

        // Verify the feature belongs to the hotel (prevents cross-hotel deletions)
        if (!feature.getHotelId().equals(hotelId)) {
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
