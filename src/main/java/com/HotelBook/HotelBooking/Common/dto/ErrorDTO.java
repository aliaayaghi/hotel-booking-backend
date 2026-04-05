package com.HotelBook.HotelBooking.Common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorDTO {

    private int status;
    private String error;                      // e.g. "REVIEW_NOT_FOUND"
    private String message;                    // human readable
    private Map<String, String> fieldErrors;   // validation errors only
    private LocalDateTime timestamp;

    // ── FACTORY METHODS ──────────────────────────────────────────────────────

    // Simple error: not found, forbidden, conflict, etc.
    public static ErrorDTO of(int status, String error, String message) {
        return ErrorDTO.builder()
                .status(status)
                .error(error)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Validation errors with field-level detail
    public static ErrorDTO withFieldErrors(int status,
                                           String error,
                                           String message,
                                           Map<String, String> fieldErrors) {
        return ErrorDTO.builder()
                .status(status)
                .error(error)
                .message(message)
                .fieldErrors(fieldErrors)
                .timestamp(LocalDateTime.now())
                .build();
    }
}