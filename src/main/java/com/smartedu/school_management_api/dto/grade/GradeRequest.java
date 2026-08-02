package com.smartedu.school_management_api.dto.grade;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GradeRequest(
        @NotBlank(message = "Grade name is required")
        @Size(max = 80, message = "Grade name must not exceed 80 characters")
        String name,

        @Min(value = 1, message = "Level must be at least 1")
        @Max(value = 30, message = "Level must not exceed 30")
        Integer levelOrder,

        @Size(max = 255, message = "Description must not exceed 255 characters")
        String description,

        Long schoolId
) {
}
