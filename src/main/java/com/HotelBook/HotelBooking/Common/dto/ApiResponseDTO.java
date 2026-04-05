package com.HotelBook.HotelBooking.Common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

// Every single endpoint in the project returns this wrapper — success or fail
// T is the actual data type e.g. ApiResponseDTO<ReviewResponseDTO>
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponseDTO<T> {

    private boolean success;
    private String message;
    private T data;              // null on error responses
    private LocalDateTime timestamp;

    public static <T> ApiResponseDTO<T> success(T data, String message) {
        return ApiResponseDTO.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Shortcut when you don't need a custom message
    public static <T> ApiResponseDTO<T> success(T data) {
        return success(data, "Operation completed successfully");
    }

    public static <T> ApiResponseDTO<T> error(String message) {
        return ApiResponseDTO.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
    }
}