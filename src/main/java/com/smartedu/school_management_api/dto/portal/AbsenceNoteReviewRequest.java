package com.smartedu.school_management_api.dto.portal;

import com.smartedu.school_management_api.entity.AbsenceNoteStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** A teacher's or admin's decision on an absence note. */
public record AbsenceNoteReviewRequest(
        @NotNull(message = "A decision is required")
        AbsenceNoteStatus status,

        @Size(max = 500, message = "Review note must not exceed 500 characters")
        String reviewNote
) {
}
