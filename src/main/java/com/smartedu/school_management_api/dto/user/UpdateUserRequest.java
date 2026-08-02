package com.smartedu.school_management_api.dto.user;

import com.smartedu.school_management_api.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Partial update: every field is optional and a null means "leave as is".
 * A blank {@code password} likewise leaves the existing hash untouched.
 */
public record UpdateUserRequest(
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,

        @Size(max = 120, message = "Full name must not exceed 120 characters")
        String fullName,

        @Email(message = "Please enter a valid email")
        @Size(max = 150)
        String email,

        @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
        String password,

        UserRole role,

        @Size(max = 30, message = "Phone number must not exceed 30 characters")
        String phoneNumber,

        @Size(max = 500)
        String avatarUrl,

        Boolean active,

        Long schoolId
) {
}
