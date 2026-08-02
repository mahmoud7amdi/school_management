package com.smartedu.school_management_api.dto.student;

import com.smartedu.school_management_api.dto.common.ReferenceResponse;
import com.smartedu.school_management_api.entity.Gender;
import com.smartedu.school_management_api.entity.StudentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record StudentResponse(
        Long id,
        String admissionNumber,
        String firstName,
        String lastName,
        String fullName,
        Gender gender,
        LocalDate dateOfBirth,
        Integer age,
        String email,
        String phoneNumber,
        String address,
        String photoUrl,
        String guardianName,
        String guardianPhone,
        String guardianEmail,
        LocalDate enrollmentDate,
        StudentStatus status,
        String statusLabel,
        ReferenceResponse grade,
        ReferenceResponse classroom,
        ReferenceResponse school,
        boolean hasUserAccount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
