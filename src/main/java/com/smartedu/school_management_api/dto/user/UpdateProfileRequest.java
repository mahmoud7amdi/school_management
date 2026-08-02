package com.smartedu.school_management_api.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * What a user may change about their own account. Deliberately excludes role,
 * active and school so self-service can never be an escalation path.
 */
public record UpdateProfileRequest(
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,

        @Size(max = 120, message = "Full name must not exceed 120 characters")
        String fullName,

        @Email(message = "Please enter a valid email")
        @Size(max = 150)
        String email,

        @Size(max = 30, message = "Phone number must not exceed 30 characters")
        String phoneNumber,

        @Size(max = 500)
        String avatarUrl,

        /** Required when setting a new password; verified against the stored hash. */
        String currentPassword,

        @Size(min = 6, max = 100, message = "New password must be at least 6 characters")
        String newPassword
) {
}
