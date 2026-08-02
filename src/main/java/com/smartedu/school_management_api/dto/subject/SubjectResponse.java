package com.smartedu.school_management_api.dto.subject;

import com.smartedu.school_management_api.dto.common.ReferenceResponse;

import java.time.LocalDateTime;

public record SubjectResponse(
        Long id,
        String name,
        String code,
        Integer weeklyHours,
        ReferenceResponse grade,
        ReferenceResponse school,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
