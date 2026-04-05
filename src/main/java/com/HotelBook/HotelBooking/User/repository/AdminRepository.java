package com.HotelBook.HotelBooking.User.repository;

import com.HotelBook.HotelBooking.User.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AdminRepository extends JpaRepository<Admin, UUID> {

    Optional<Admin> findByUser_Id(UUID userId);
}