package com.smartedu.school_management_api.dto.attendance;

import com.smartedu.school_management_api.entity.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * One attendance mark. The classroom is derived from the request's class scope and
 * the school from the tenant, so neither is part of the payload.
 */
public record AttendanceRequest(
        @NotNull(message = "Attendance date is required")
        LocalDate attendanceDate,

        @NotNull(message = "Status is required")
        AttendanceStatus status,

        @Size(max = 255, message = "Remarks must not exceed 255 characters")
        String remarks,

        @NotNull(message = "Student is required")
        Long studentId,

        @NotNull(message = "Classroom is required")
        Long classroomId,

        /** Period-level registers set this; whole-day registers leave it null. */
        Long subjectId,

        /** Who took the register; null falls back to the recorded-by teacher. */
        Long recordedById
) {
}
