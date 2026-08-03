package com.smartedu.school_management_api.dto.portal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * A family's explanation for one day's absence.
 *
 * <p>{@code studentId} is required from a parent, who may have several children, and
 * ignored for a student, who can only ever file for themselves.
 */
public record AbsenceNoteRequest(
        Long studentId,

        @NotNull(message = "The date of the absence is required")
        LocalDate absenceDate,

        @NotBlank(message = "Please give a reason for the absence")
        @Size(max = 1000, message = "Reason must not exceed 1000 characters")
        String reason
) {
}
