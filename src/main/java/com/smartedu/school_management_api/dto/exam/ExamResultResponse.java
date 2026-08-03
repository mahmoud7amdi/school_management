package com.smartedu.school_management_api.dto.exam;

import com.smartedu.school_management_api.dto.common.ReferenceResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Percentage, pass flag and letter grade are derived by the entity, not stored. */
public record ExamResultResponse(
        Long id,
        BigDecimal marksObtained,
        Boolean absent,
        BigDecimal percentage,
        Boolean passed,
        String gradeLetter,
        String remarks,
        ReferenceResponse exam,
        BigDecimal maxMarks,
        ReferenceResponse student,
        String admissionNumber,
        ReferenceResponse school,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
