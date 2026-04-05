package com.HotelBook.HotelBooking.HotelAccessibility;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HotelAccessibilityRepository extends JpaRepository<HotelAccessibility, UUID> {

    // ── Used by HotelAccessibilityService ─────────────────────────────────────
    List<HotelAccessibility> findByHotelId(UUID hotelId);

    // ── Used by M3's search/filter service ────────────────────────────────────
    // e.g. find all wheelchair-accessible features for a hotel
    List<HotelAccessibility> findByHotelIdAndLevel(UUID hotelId, AccessibilityLevel level);

    // Used when deleting a feature — verify ownership (hotelId matches)
    boolean existsByIdAndHotelId(UUID id, UUID hotelId);
}