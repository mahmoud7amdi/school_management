package com.smartedu.school_management_api.dto.exam;

import com.smartedu.school_management_api.dto.common.ReferenceResponse;
import com.smartedu.school_management_api.entity.ExamType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record ExamResponse(
        Long id,
        String title,
        ExamType examType,
        String examTypeLabel,
        LocalDate examDate,
        LocalTime startTime,
        Integer durationMinutes,
        BigDecimal maxMarks,
        BigDecimal passMarks,
        String description,
        ReferenceResponse subject,
        ReferenceResponse grade,
        ReferenceResponse classroom,
        ReferenceResponse academicYear,
        ReferenceResponse school,
        long resultCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
