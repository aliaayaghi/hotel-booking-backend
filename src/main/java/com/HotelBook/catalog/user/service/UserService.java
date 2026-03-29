package com.HotelBook.catalog.user.service;


import com.HotelBook.catalog.user.dto.request.ChangePasswordRequest;
import com.HotelBook.catalog.user.dto.request.UpdateProfileRequest;
import com.HotelBook.catalog.user.dto.response.UserResponse;

import java.util.UUID;

public interface UserService {

    UserResponse getCurrentUser(UUID userId);

    UserResponse updateProfile(UUID userId, UpdateProfileRequest request);

    void changePassword(UUID userId, ChangePasswordRequest request);
}
