package com.smartedu.school_management_api.dto.exam;

import com.smartedu.school_management_api.entity.ExamType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/** The school and grade are derived from the subject, keeping the tenant implicit. */
public record ExamRequest(
        @NotBlank(message = "Exam title is required")
        @Size(max = 150, message = "Exam title must not exceed 150 characters")
        String title,

        @NotNull(message = "Exam type is required")
        ExamType examType,

        @NotNull(message = "Exam date is required")
        LocalDate examDate,

        LocalTime startTime,

        @Min(value = 5, message = "Duration must be at least 5 minutes")
        @Max(value = 600, message = "Duration must not exceed 600 minutes")
        Integer durationMinutes,

        @NotNull(message = "Maximum marks is required")
        @DecimalMin(value = "1.0", message = "Maximum marks must be at least 1")
        @Digits(integer = 4, fraction = 2, message = "Maximum marks is out of range")
        BigDecimal maxMarks,

        @NotNull(message = "Pass marks is required")
        @DecimalMin(value = "0.0", message = "Pass marks cannot be negative")
        @Digits(integer = 4, fraction = 2, message = "Pass marks is out of range")
        BigDecimal passMarks,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        @NotNull(message = "Subject is required")
        Long subjectId,

        /** Optional: a grade-wide paper is not tied to one class. */
        Long classroomId,

        @NotNull(message = "Academic year is required")
        Long academicYearId
) {
}
