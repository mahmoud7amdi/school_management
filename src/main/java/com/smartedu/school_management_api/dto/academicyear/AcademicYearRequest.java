package com.smartedu.school_management_api.dto.academicyear;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Create/update payload for an academic year.
 *
 * <p>{@code schoolId} is ignored: the caller is always a school admin, pinned to its own
 * school. The end-after-start rule is checked in the service.
 */
public record AcademicYearRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 50, message = "Name must not exceed 50 characters")
        String name,

        @NotNull(message = "Start date is required")
        LocalDate startDate,

        @NotNull(message = "End date is required")
        LocalDate endDate,

        Boolean current,

        Long schoolId
) {
}
