package com.HotelBook.HotelBooking.Payment;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {


    Optional<Payment> findByBookingId(UUID bookingId);


    boolean existsByBookingId(UUID bookingId);

    Optional<Payment> findByBookingIdAndCustomerId(UUID bookingId, UUID customerId);

    List<Payment> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    List<Payment> findByStatus(PaymentStatus status);

    @Modifying
    @Query("DELETE FROM Payment p WHERE p.booking.id IN :bookingIds")
    void deleteByBookingIdIn(@Param("bookingIds") List<UUID> bookingIds);}
