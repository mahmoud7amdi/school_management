package com.smartedu.school_management_api.dto.section;

import com.smartedu.school_management_api.dto.common.ReferenceResponse;

import java.time.LocalDateTime;

public record SectionResponse(
        Long id,
        String name,
        Integer capacity,
        String description,
        ReferenceResponse grade,
        ReferenceResponse school,
        ReferenceResponse sectionHead,
        long classroomCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
