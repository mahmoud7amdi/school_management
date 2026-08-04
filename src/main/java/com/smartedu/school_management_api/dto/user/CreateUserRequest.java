package com.smartedu.school_management_api.dto.user;

import com.smartedu.school_management_api.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Inbound payload for user creation.
 *
 * <p>{@code schoolId} is honoured only when a super admin appoints a school admin; a
 * school admin always gets its own school, so a spoofed id in the body is ignored.
 */
public record CreateUserRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,

        @NotBlank(message = "Full name is required")
        @Size(max = 120, message = "Full name must not exceed 120 characters")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "Please enter a valid email")
        @Size(max = 150)
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
        String password,

        @NotNull(message = "Role is required")
        UserRole role,

        @Size(max = 30, message = "Phone number must not exceed 30 characters")
        String phoneNumber,

        @Size(max = 500)
        String avatarUrl,

        Long schoolId
) {
}
