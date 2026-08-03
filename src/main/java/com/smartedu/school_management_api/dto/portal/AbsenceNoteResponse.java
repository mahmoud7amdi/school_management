package com.smartedu.school_management_api.dto.portal;

import com.smartedu.school_management_api.dto.common.ReferenceResponse;
import com.smartedu.school_management_api.entity.AbsenceNoteStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AbsenceNoteResponse(
        Long id,
        LocalDate absenceDate,
        String reason,
        AbsenceNoteStatus status,
        String statusLabel,
        ReferenceResponse student,
        String admissionNumber,
        ReferenceResponse classroom,
        ReferenceResponse submittedBy,
        ReferenceResponse reviewedBy,
        LocalDateTime reviewedAt,
        String reviewNote,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
