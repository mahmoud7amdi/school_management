package com.smartedu.school_management_api.dto.auth;

import com.smartedu.school_management_api.dto.user.UserResponse;

/** Issued token plus the profile the dashboard needs to render its shell. */
public record LoginResponse(String token, String tokenType, long expiresInSeconds, UserResponse user) {

    public static LoginResponse of(String token, long expiresInSeconds, UserResponse user) {
        return new LoginResponse(token, "Bearer", expiresInSeconds, user);
    }
}
