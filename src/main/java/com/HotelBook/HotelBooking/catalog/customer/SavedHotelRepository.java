package com.HotelBook.HotelBooking.catalog.customer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface SavedHotelRepository extends JpaRepository<SavedHotel, UUID> {

    // Fetch all saved hotels for a customer, sorted newest first
    List<SavedHotel> findByCustomerIdOrderBySavedAtDesc(UUID customerId);

    // Existence check — used before saving to detect duplicates
    boolean existsByCustomerIdAndHotelId(UUID customerId, UUID hotelId);

    // Delete by composite key — used by unsaveHotel()
    @Modifying
    @Transactional
    void deleteByCustomerIdAndHotelId(UUID customerId, UUID hotelId);

    // Count helper — useful for dashboard stats
    long countByCustomerId(UUID customerId);
}
