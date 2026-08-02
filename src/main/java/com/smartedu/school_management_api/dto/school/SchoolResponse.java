package com.smartedu.school_management_api.dto.school;

import java.time.LocalDateTime;

public record SchoolResponse(
        Long id,
        String name,
        String address,
        String phoneNumber,
        String email,
        String logoUrl,
        String website,
        Boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
