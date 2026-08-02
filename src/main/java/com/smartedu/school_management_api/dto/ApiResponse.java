package com.smartedu.school_management_api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

/**
 * Uniform envelope for every JSON response.
 *
 * <p>{@code status} mirrors the HTTP status so the front end can branch on a single
 * field, and {@code fieldErrors} carries per-field validation messages.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        int status,
        boolean success,
        String message,
        T data,
        String error,
        Map<String, String> fieldErrors,
        Instant timestamp
) {

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(200, true, message, data, null, null, Instant.now());
    }

    public static <T> ApiResponse<T> created(T data, String message) {
        return new ApiResponse<>(201, true, message, data, null, null, Instant.now());
    }

    public static <T> ApiResponse<T> error(int status, String message, String error) {
        return new ApiResponse<>(status, false, message, null, error, null, Instant.now());
    }

    public static <T> ApiResponse<T> validationError(String message, Map<String, String> fieldErrors) {
        return new ApiResponse<>(400, false, message, null, "Validation failed", fieldErrors, Instant.now());
    }
}
