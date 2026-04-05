package com.HotelBook.HotelBooking.Admin;


import com.HotelBook.HotelBooking.Hotel.HotelStatus;
import com.HotelBook.HotelBooking.Hotel.HotelResponse;
import com.HotelBook.HotelBooking.User.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminService {

    // ── Hotel management ─────────────────────────────────────────────────────
    Page<HotelResponse> getAllHotels(HotelStatus status, Pageable pageable);

    HotelResponse approveHotel(UUID hotelId);

    HotelResponse rejectHotel(UUID hotelId, String reason);

    void deleteHotel(UUID hotelId);

    // ── User management ──────────────────────────────────────────────────────
    Page<UserResponse> getAllUsers(Pageable pageable);

    UserResponse suspendUser(UUID userId, String reason);

    UserResponse unsuspendUser(UUID userId);

    // ── Dashboard ────────────────────────────────────────────────────────────
    AdminDashboardResponse getDashboardStats();
}