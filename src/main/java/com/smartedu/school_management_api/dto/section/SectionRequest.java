package com.smartedu.school_management_api.dto.section;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** The school is derived from the grade, so it is not part of the payload. */
public record SectionRequest(
        @NotBlank(message = "Section name is required")
        @Size(max = 50, message = "Section name must not exceed 50 characters")
        String name,

        @Min(value = 1, message = "Capacity must be at least 1")
        @Max(value = 500, message = "Capacity must not exceed 500")
        Integer capacity,

        @Size(max = 255, message = "Description must not exceed 255 characters")
        String description,

        @NotNull(message = "Grade is required")
        Long gradeId,

        /** Optional section head; must be a teacher in the same school. */
        Long sectionHeadId
) {
}
