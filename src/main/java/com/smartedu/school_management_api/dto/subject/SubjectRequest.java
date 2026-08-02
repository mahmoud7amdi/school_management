package com.smartedu.school_management_api.dto.subject;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** The school is derived from the grade, so it is not part of the payload. */
public record SubjectRequest(
        @NotBlank(message = "Subject name is required")
        @Size(max = 100, message = "Subject name must not exceed 100 characters")
        String name,

        @Size(max = 20, message = "Code must not exceed 20 characters")
        String code,

        @Min(value = 1, message = "Weekly hours must be at least 1")
        @Max(value = 60, message = "Weekly hours must not exceed 60")
        Integer weeklyHours,

        @NotNull(message = "Grade is required")
        Long gradeId
) {
}
