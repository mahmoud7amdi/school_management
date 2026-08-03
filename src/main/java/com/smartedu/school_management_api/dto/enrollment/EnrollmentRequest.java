package com.smartedu.school_management_api.dto.enrollment;

import com.smartedu.school_management_api.entity.EnrollmentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * The school is derived from the student, so a caller cannot enrol into a school
 * they cannot reach.
 */
public record EnrollmentRequest(
        @Size(max = 20, message = "Roll number must not exceed 20 characters")
        String rollNumber,

        @NotNull(message = "Enrollment date is required")
        LocalDate enrollmentDate,

        LocalDate completionDate,

        @NotNull(message = "Status is required")
        EnrollmentStatus status,

        @Size(max = 500, message = "Remarks must not exceed 500 characters")
        String remarks,

        @NotNull(message = "Student is required")
        Long studentId,

        @NotNull(message = "Academic year is required")
        Long academicYearId,

        @NotNull(message = "Grade is required")
        Long gradeId,

        /** Optional: a student can be enrolled before being placed in a class. */
        Long classroomId
) {
}
