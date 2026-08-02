package com.smartedu.school_management_api.dto.user;

import com.smartedu.school_management_api.dto.common.ReferenceResponse;
import com.smartedu.school_management_api.entity.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

/** Outbound view of a user. The password hash has no representation here by design. */
public record UserResponse(
        UUID id,
        String username,
        String fullName,
        String email,
        String phoneNumber,
        String avatarUrl,
        UserRole role,
        String roleLabel,
        Boolean active,
        ReferenceResponse school,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
