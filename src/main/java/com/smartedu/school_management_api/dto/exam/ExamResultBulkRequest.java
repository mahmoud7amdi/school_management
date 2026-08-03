package com.smartedu.school_management_api.dto.exam;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

/**
 * Marks for a whole exam in one call.
 *
 * <p>The marks grid re-submits every row on each save, so the service upserts on
 * (exam, student) rather than inserting — see {@code uk_exam_result_exam_student}.
 */
public record ExamResultBulkRequest(
        @NotNull(message = "Exam is required")
        Long examId,

        @NotEmpty(message = "At least one result is required")
        @Valid
        List<Entry> entries
) {

    public record Entry(
            @NotNull(message = "Student is required")
            Long studentId,

            /** Null means "not marked yet"; absent rows carry no marks. */
            @DecimalMin(value = "0.0", message = "Marks cannot be negative")
            @Digits(integer = 4, fraction = 2, message = "Marks value is out of range")
            BigDecimal marksObtained,

            Boolean absent,

            @Size(max = 255, message = "Remarks must not exceed 255 characters")
            String remarks
    ) {
    }
}
