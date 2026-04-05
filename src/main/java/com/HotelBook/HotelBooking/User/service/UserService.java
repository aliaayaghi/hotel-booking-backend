package com.HotelBook.HotelBooking.User.service;


import com.HotelBook.HotelBooking.User.dto.request.ChangePasswordRequest;
import com.HotelBook.HotelBooking.User.dto.request.UpdateProfileRequest;
import com.HotelBook.HotelBooking.User.dto.response.UserResponse;

import java.util.UUID;

public interface UserService {

    UserResponse getCurrentUser(UUID userId);

    UserResponse updateProfile(UUID userId, UpdateProfileRequest request);

    void changePassword(UUID userId, ChangePasswordRequest request);
}
