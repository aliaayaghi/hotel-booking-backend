package com.HotelBook.HotelBooking.HotelPhoto;

import com.HotelBook.HotelBooking.Hotel.Hotel;
import com.HotelBook.HotelBooking.Hotel.HotelRepository;
import com.HotelBook.HotelBooking.Common.exception.ResourceNotFoundException;
import com.HotelBook.HotelBooking.Common.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotelPhotoServiceImpl implements HotelPhotoService {

    private final HotelPhotoRepository photoRepository;
    private final HotelRepository hotelRepository;
    private final HotelPhotoMapper photoMapper;

    @Override
    @Transactional(readOnly = true)
    public List<HotelPhotoResponse> getPhotos(UUID hotelId) {
        verifyHotelExists(hotelId);
        return photoMapper.toResponseList(photoRepository.findByHotelIdOrderByOrderAsc(hotelId));
    }

    @Override
    @Transactional
    public HotelPhotoResponse addPhoto(UUID hotelId, UUID managerId, CreatePhotoRequest request) {
        Hotel hotel = getHotelOrThrow(hotelId);
        validateManagerOwnership(managerId, hotelId);

        if (request.isCover()) {
            photoRepository.clearCoverByHotelId(hotelId);
            log.debug("Cleared existing cover for hotel {} before setting new cover", hotelId);
        }

        long photoCount = photoRepository.countByHotelId(hotelId);

        HotelPhoto photo = HotelPhoto.builder()
                .hotel(hotel)
                .url(request.getUrl())
                .caption(request.getCaption())
                .isCover(request.isCover())
                .order((int) photoCount)
                .build();

        photo = photoRepository.save(photo);
        log.info("Added photo {} to hotel {} (cover={})", photo.getId(), hotelId, photo.isCover());
        return photoMapper.toResponse(photo);
    }

    @Override
    @Transactional
    public void deletePhoto(UUID hotelId, UUID photoId, UUID managerId) {
        verifyHotelExists(hotelId);
        validateManagerOwnership(managerId, hotelId);

        HotelPhoto photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("HotelPhoto", photoId));

        if (!photo.getHotel().getId().equals(hotelId)) {
            throw new ResourceNotFoundException("HotelPhoto", photoId);
        }

        photoRepository.delete(photo);
        log.info("Deleted photo {} from hotel {}", photoId, hotelId);
    }

    @Override
    @Transactional
    public List<HotelPhotoResponse> reorderPhotos(UUID hotelId, UUID managerId, ReorderPhotosRequest request) {
        verifyHotelExists(hotelId);
        validateManagerOwnership(managerId, hotelId);

        List<UUID> newOrder = request.getPhotoIds();
        List<HotelPhoto> existingPhotos = photoRepository.findByHotelId(hotelId);

        if (existingPhotos.size() != newOrder.size()) {
            throw new IllegalArgumentException(
                    "photoIds must include all " + existingPhotos.size() + " photos for this hotel. " +
                            "Received " + newOrder.size() + " IDs."
            );
        }

        Map<UUID, HotelPhoto> photoById = existingPhotos.stream()
                .collect(Collectors.toMap(HotelPhoto::getId, Function.identity()));

        for (UUID id : newOrder) {
            if (!photoById.containsKey(id)) {
                throw new ResourceNotFoundException("HotelPhoto", id);
            }
        }

        List<HotelPhoto> updated = new ArrayList<>();
        for (int i = 0; i < newOrder.size(); i++) {
            HotelPhoto photo = photoById.get(newOrder.get(i));
            photo.setOrder(i);
            updated.add(photo);
        }

        List<HotelPhoto> saved = photoRepository.saveAll(updated);
        log.info("Reordered {} photos for hotel {}", saved.size(), hotelId);

        return saved.stream()
                .sorted((a, b) -> Integer.compare(a.getOrder(), b.getOrder()))
                .map(photoMapper::toResponse)
                .collect(Collectors.toList());
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private Hotel getHotelOrThrow(UUID hotelId) {
        return hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel", hotelId));
    }

    private void verifyHotelExists(UUID hotelId) {
        if (!hotelRepository.existsById(hotelId)) {
            throw new ResourceNotFoundException("Hotel", hotelId);
        }
    }

    private void validateManagerOwnership(UUID managerId, UUID hotelId) {
        if (!hotelRepository.existsByIdAndManager_Id(hotelId, managerId)) {
            throw new UnauthorizedException(
                    "You do not have permission to modify photos for hotel: " + hotelId
            );
        }
    }
}