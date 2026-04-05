package com.HotelBook.HotelBooking.HotelPolicy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PetPolicyRepository extends JpaRepository<PetPolicy, UUID> {

    Optional<PetPolicy> findByHotelId(UUID hotelId);
}
