package com.smartedu.school_management_api.dto.attendance;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

/**
 * A whole register submitted in one call: one class, one date, every student.
 *
 * <p>The register screen re-submits the same day whenever a mark is corrected, so
 * the service upserts on (student, date) rather than inserting — see
 * {@code uk_attendance_student_date}.
 */
public record AttendanceBulkRequest(
        @NotNull(message = "Classroom is required")
        Long classroomId,

        @NotNull(message = "Attendance date is required")
        LocalDate attendanceDate,

        /** Period-level registers set this; whole-day registers leave it null. */
        Long subjectId,

        Long recordedById,

        @NotEmpty(message = "At least one student mark is required")
        @Valid
        List<Entry> entries
) {

    public record Entry(
            @NotNull(message = "Student is required")
            Long studentId,

            @NotNull(message = "Status is required")
            com.smartedu.school_management_api.entity.AttendanceStatus status,

            @Size(max = 255, message = "Remarks must not exceed 255 characters")
            String remarks
    ) {
    }
}
