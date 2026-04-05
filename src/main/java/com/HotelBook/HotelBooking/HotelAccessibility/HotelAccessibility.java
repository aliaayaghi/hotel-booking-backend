package com.HotelBook.HotelBooking.HotelAccessibility;


import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * HotelAccessibility Entity
 *
 * Stores accessibility features for a hotel — one row per feature.
 * A hotel can have multiple accessibility records (e.g. wheelchair ramp, elevator, accessible bathroom).
 *
 * Table: hotel_accessibility
 * Relationship: @ManyToOne Hotel (hotelId FK) — not mapped as JPA association, stored as plain UUID.
 *
 * Used by M3's search filter to determine which hotels are wheelchair-accessible etc.
 * M3 will call HotelAccessibilityRepository.findByHotelIdAndLevel() for filtering.
 */
@Entity
@Table(
        name = "hotel_accessibility",
        indexes = @Index(name = "idx_hotel_accessibility_hotel_id", columnList = "hotelId")
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotelAccessibility {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    // FK → hotels.id — stored as a plain UUID column (no JPA @ManyToOne join)
    // This avoids loading the full Hotel entity on every accessibility query.
    @Column(nullable = false)
    private UUID hotelId;

    // e.g. "Wheelchair Ramp", "Elevator", "Accessible Bathroom", "Hearing Loop"
    @Column(nullable = false)
    private String feature;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccessibilityLevel level;

    // Optional free-text details — e.g. "Ramp available at the side entrance only"
    @Column(columnDefinition = "TEXT")
    private String description;
}