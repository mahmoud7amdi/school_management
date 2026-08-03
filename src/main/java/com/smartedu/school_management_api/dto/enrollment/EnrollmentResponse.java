package com.smartedu.school_management_api.dto.enrollment;

import com.smartedu.school_management_api.dto.common.ReferenceResponse;
import com.smartedu.school_management_api.entity.EnrollmentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record EnrollmentResponse(
        Long id,
        String rollNumber,
        LocalDate enrollmentDate,
        LocalDate completionDate,
        EnrollmentStatus status,
        String statusLabel,
        String remarks,
        ReferenceResponse student,
        String admissionNumber,
        ReferenceResponse academicYear,
        ReferenceResponse grade,
        ReferenceResponse classroom,
        ReferenceResponse school,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
