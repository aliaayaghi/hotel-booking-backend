package com.HotelBook.HotelBooking.User.service;

import com.HotelBook.HotelBooking.User.dto.request.LoginRequest;
import com.HotelBook.HotelBooking.User.dto.request.RegisterRequest;
import com.HotelBook.HotelBooking.User.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void logout(String token);
}
