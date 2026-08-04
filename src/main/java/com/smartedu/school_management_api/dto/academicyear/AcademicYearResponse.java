package com.smartedu.school_management_api.dto.academicyear;

import com.smartedu.school_management_api.dto.common.ReferenceResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record
AcademicYearResponse(
        Long id,
        String name,

        LocalDate startDate,
        LocalDate endDate,
        Boolean current,
        ReferenceResponse school,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
