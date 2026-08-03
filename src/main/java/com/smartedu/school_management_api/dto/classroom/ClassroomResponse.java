package com.smartedu.school_management_api.dto.classroom;

import com.smartedu.school_management_api.dto.common.ReferenceResponse;

import java.time.LocalDateTime;

public record ClassroomResponse(
        Long id,
        String name,
        Integer capacity,
        String roomNumber,
        ReferenceResponse grade,
        ReferenceResponse academicYear,
        ReferenceResponse section,
        String classTeacherName,
        ReferenceResponse school,
        long studentCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
