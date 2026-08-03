package com.smartedu.school_management_api.dto.classroom;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ClassroomRequest(
        @NotBlank(message = "Classroom name is required")
        @Size(max = 80, message = "Classroom name must not exceed 80 characters")
        String name,

        @Min(value = 1, message = "Capacity must be at least 1")
        @Max(value = 500, message = "Capacity must not exceed 500")
        Integer capacity,

        @Size(max = 50, message = "Room number must not exceed 50 characters")
        String roomNumber,

        @NotNull(message = "Grade is required")
        Long gradeId,

        @NotNull(message = "Academic year is required")
        Long academicYearId,

        /** Optional homeroom teacher; must be a TEACHER in the same school. */
        UUID classTeacherId,

        /** Optional grade division; must belong to the same grade and school. */
        Long sectionId
) {
}
