package com.smartedu.school_management_api.dto.student;

import com.smartedu.school_management_api.entity.Gender;
import com.smartedu.school_management_api.entity.StudentStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Create/update payload for a student.
 *
 * <p>The school comes from the chosen grade (and, for a school admin, from the caller),
 * so it is never accepted from the client. {@code classroomId} may be null to enrol a
 * student before placing them in a class.
 */
public record StudentRequest(
        @NotBlank(message = "Admission number is required")
        @Size(max = 40, message = "Admission number must not exceed 40 characters")
        String admissionNumber,

        @NotBlank(message = "First name is required")
        @Size(max = 60, message = "First name must not exceed 60 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 60, message = "Last name must not exceed 60 characters")
        String lastName,

        @NotNull(message = "Gender is required")
        Gender gender,

        @NotNull(message = "Date of birth is required")
        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        @Email(message = "Please enter a valid email")
        @Size(max = 150)
        String email,

        @Size(max = 30, message = "Phone number must not exceed 30 characters")
        String phoneNumber,

        @Size(max = 255, message = "Address must not exceed 255 characters")
        String address,

        @Size(max = 500)
        String photoUrl,

        @Size(max = 120, message = "Guardian name must not exceed 120 characters")
        String guardianName,

        @Size(max = 30, message = "Guardian phone must not exceed 30 characters")
        String guardianPhone,

        @Email(message = "Please enter a valid guardian email")
        @Size(max = 150)
        String guardianEmail,

        @NotNull(message = "Enrollment date is required")
        LocalDate enrollmentDate,

        StudentStatus status,

        @NotNull(message = "Grade is required")
        Long gradeId,

        Long classroomId
) {
}
