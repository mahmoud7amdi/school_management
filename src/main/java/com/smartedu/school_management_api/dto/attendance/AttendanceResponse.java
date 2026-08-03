package com.smartedu.school_management_api.dto.attendance;

import com.smartedu.school_management_api.dto.common.ReferenceResponse;
import com.smartedu.school_management_api.entity.AttendanceStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AttendanceResponse(
        Long id,
        LocalDate attendanceDate,
        AttendanceStatus status,
        String statusLabel,
        String remarks,
        ReferenceResponse student,
        ReferenceResponse classroom,
        ReferenceResponse subject,
        ReferenceResponse recordedBy,
        ReferenceResponse school,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
