package com.smartedu.school_management_api.dto.fee;

import com.smartedu.school_management_api.dto.common.ReferenceResponse;
import com.smartedu.school_management_api.entity.PaymentMethod;
import com.smartedu.school_management_api.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record FeePaymentResponse(
        Long id,
        String receiptNumber,
        BigDecimal amountPaid,
        LocalDate paymentDate,
        PaymentMethod method,
        String methodLabel,
        PaymentStatus status,
        String statusLabel,
        String remarks,
        ReferenceResponse student,
        String admissionNumber,
        ReferenceResponse feeStructure,
        ReferenceResponse school,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
