package com.HotelBook.HotelBooking.common;



import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;


@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorDTO>> handleValidationException(
            MethodArgumentNotValidException ex) {

        List<ErrorDTO.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> new ErrorDTO.FieldError(e.getField(), e.getDefaultMessage()))
                .collect(Collectors.toList());

        ErrorDTO errorDTO = new ErrorDTO(400, fieldErrors);
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Validation failed: " + fieldErrors.size() + " error(s)"));
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<ErrorDTO>> handleConstraintViolation(
            ConstraintViolationException ex) {

        List<ErrorDTO.FieldError> errors = ex.getConstraintViolations()
                .stream()
                .map(v -> new ErrorDTO.FieldError(
                        v.getPropertyPath().toString(), v.getMessage()))
                .collect(Collectors.toList());

        ErrorDTO errorDTO = new ErrorDTO(400, errors);
        return ResponseEntity.badRequest().body(ApiResponse.error("Validation failed"));
    }


    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<ErrorDTO>> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<ErrorDTO>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
    }


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorDTO>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
    }


    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<ErrorDTO>> handleConflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(ex.getMessage()));
    }


    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<ErrorDTO>> handleRuntimeException(RuntimeException ex) {
        // In production you'd log this: log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred: " + ex.getMessage()));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorDTO>> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Internal server error"));
    }
}
