package com.smartedu.school_management_api.dto.fee;

import com.smartedu.school_management_api.dto.common.ReferenceResponse;
import com.smartedu.school_management_api.entity.FeeType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record FeeStructureResponse(
        Long id,
        String name,
        FeeType feeType,
        String feeTypeLabel,
        BigDecimal amount,
        LocalDate dueDate,
        String description,
        ReferenceResponse grade,
        ReferenceResponse academicYear,
        ReferenceResponse school,
        /** Money actually collected against this item, across all students. */
        BigDecimal totalCollected,
        long paymentCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
