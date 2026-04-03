package com.HotelBook.HotelBooking.catalog.customer;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * SavedHotel — the customer's wishlist / favourites.
 *
 * ⚠️  TEAM NOTE:
 * The saved_hotels TABLE is part of your (M1) module. However, M2's booking
 * module may also need to read it. Coordinate with M2 to confirm they will
 * reference this entity rather than creating a duplicate.
 *
 * The composite unique constraint prevents a customer from saving the same
 * hotel twice — any duplicate attempt will throw a DataIntegrityViolationException
 * which CustomerServiceImpl converts to ConflictException.
 */
@Entity
@Table(
        name = "saved_hotels",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_saved_hotels_customer_hotel",
                columnNames = {"customer_id", "hotel_id"}
        )
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

    // FK → users.id (must belong to a CUSTOMER role user)
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    // FK → hotels.id
    @Column(name = "hotel_id", nullable = false)
    private UUID hotelId;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant savedAt;
}