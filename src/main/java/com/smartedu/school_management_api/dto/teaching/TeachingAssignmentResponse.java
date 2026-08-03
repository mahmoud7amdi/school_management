package com.smartedu.school_management_api.dto.teaching;

import com.smartedu.school_management_api.dto.common.ReferenceResponse;

import java.time.LocalDateTime;

public record TeachingAssignmentResponse(
        Long id,
        ReferenceResponse teacher,
        ReferenceResponse classroom,
        /** Null for a whole-class assignment. */
        ReferenceResponse subject,
        ReferenceResponse grade,
        ReferenceResponse academicYear,
        ReferenceResponse school,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
