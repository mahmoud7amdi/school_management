package com.smartedu.school_management_api.dto.fee;

import com.smartedu.school_management_api.dto.common.ReferenceResponse;
import com.smartedu.school_management_api.entity.SettlementStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * What one student owes on one fee item.
 *
 * <p>Assembled by the service from the fee amount and that student's completed
 * payments; nothing here is persisted, so the ledger cannot drift from the money.
 */
public record StudentFeeLedgerResponse(
        ReferenceResponse student,
        String admissionNumber,
        ReferenceResponse feeStructure,
        String feeTypeLabel,
        LocalDate dueDate,
        BigDecimal amountDue,
        BigDecimal amountPaid,
        BigDecimal balance,
        SettlementStatus settlementStatus,
        String settlementStatusLabel,
        boolean overdue
) {
}
