package com.smartedu.school_management_api.dto.teacher;

import com.smartedu.school_management_api.dto.user.AccountCredentials;
import com.smartedu.school_management_api.entity.Gender;
import com.smartedu.school_management_api.entity.TeacherStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

/**
 * Payload for creating or updating a teacher.
 *
 * <p>The school is derived from the employee context (tenant), never from the body.
 * {@code subjectIds} are the subjects this teacher is qualified to teach.
 *
 * <p>{@code account} is used on create only, where it provisions the teacher's login in the
 * same transaction. On update it is ignored: changing someone's employment record should not
 * silently rewrite their credentials, so account changes go through the administrator pages.
 */
public record TeacherRequest(
        @NotBlank(message = "Employee number is required")
        @Size(max = 40, message = "Employee number must not exceed 40 characters")
        String employeeNumber,

        @NotBlank(message = "First name is required")
        @Size(max = 60, message = "First name must not exceed 60 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 60, message = "Last name must not exceed 60 characters")
        String lastName,

        @NotNull(message = "Gender is required")
        Gender gender,

        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email")
        @Size(max = 150)
        String email,

        @Size(max = 30)
        String phoneNumber,

        @Size(max = 255)
        String address,

        @Size(max = 150, message = "Qualification must not exceed 150 characters")
        String qualification,

        @Size(max = 150, message = "Specialization must not exceed 150 characters")
        String specialization,

        @NotNull(message = "Hire date is required")
        LocalDate hireDate,

        @NotNull(message = "Status is required")
        TeacherStatus status,

        Set<Long> subjectIds,

        /** Sign-in details for the account created with this teacher. Create only. */
        @Valid
        AccountCredentials account
) {
}
