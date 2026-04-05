package com.HotelBook.HotelBooking.SavedHotel;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * SavedHotel — customer wishlist/favourites.
 *
 * Owned by M2 (savedhotel package).
 * M1's CustomerServiceImpl reads/writes via SavedHotelRepository.
 * No @ManyToOne joins — module independence via plain UUIDs.
 */
@Entity
@Table(
        name = "saved_hotels",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_saved_hotel_customer_hotel",
                columnNames = {"customer_id", "hotel_id"}
        ),
        indexes = {
                @Index(name = "idx_saved_hotel_customer", columnList = "customer_id"),
                @Index(name = "idx_saved_hotel_hotel",    columnList = "hotel_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavedHotel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(name = "customer_id", nullable = false, updatable = false)
    private UUID customerId;

    @Column(name = "hotel_id", nullable = false, updatable = false)
    private UUID hotelId;

    /** Optional personal note — e.g. "Visit in summer". Max 500 chars. */
    @Column(length = 500)
    private String notes;

    @CreationTimestamp
    @Column(name = "saved_at", updatable = false, nullable = false)
    private Instant savedAt;
}