package com.smartedu.school_management_api.dto.grade;

import com.smartedu.school_management_api.dto.common.ReferenceResponse;

import java.time.LocalDateTime;

public record GradeResponse(
        Long id,
        String name,
        Integer levelOrder,
        String description,
        ReferenceResponse school,
        long studentCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
