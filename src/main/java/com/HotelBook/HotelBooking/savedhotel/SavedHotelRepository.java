package com.HotelBook.HotelBooking.savedhotel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SavedHotelRepository extends JpaRepository<SavedHotel, UUID> {

    // Used by M1 CustomerServiceImpl — newest first
    List<SavedHotel> findByCustomerIdOrderBySavedAtDesc(UUID customerId);

    Optional<SavedHotel> findByCustomerIdAndHotelId(UUID customerId, UUID hotelId);

    boolean existsByCustomerIdAndHotelId(UUID customerId, UUID hotelId);

    @Modifying
    @Transactional
    void deleteByCustomerIdAndHotelId(UUID customerId, UUID hotelId);

    long countByCustomerId(UUID customerId);
    long countByHotelId(UUID hotelId);   // useful for M2 hotel stats
}