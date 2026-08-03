package com.smartedu.school_management_api.dto.teacher;

import com.smartedu.school_management_api.dto.common.ReferenceResponse;
import com.smartedu.school_management_api.entity.Gender;
import com.smartedu.school_management_api.entity.TeacherStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record TeacherResponse(
        Long id,
        String employeeNumber,
        String firstName,
        String lastName,
        String fullName,
        Gender gender,
        LocalDate dateOfBirth,
        String email,
        String phoneNumber,
        String address,
        String qualification,
        String specialization,
        LocalDate hireDate,
        TeacherStatus status,
        String statusLabel,
        List<ReferenceResponse> subjects,
        ReferenceResponse school,
        boolean hasUserAccount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
