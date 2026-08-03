package com.smartedu.school_management_api.dto.fee;

import com.smartedu.school_management_api.entity.PaymentMethod;
import com.smartedu.school_management_api.entity.PaymentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** The school is derived from the student, keeping the tenant implicit. */
public record FeePaymentRequest(
        @NotBlank(message = "Receipt number is required")
        @Size(max = 40, message = "Receipt number must not exceed 40 characters")
        String receiptNumber,

        @NotNull(message = "Amount paid is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        @Digits(integer = 10, fraction = 2, message = "Amount is out of range")
        BigDecimal amountPaid,

        @NotNull(message = "Payment date is required")
        LocalDate paymentDate,

        @NotNull(message = "Payment method is required")
        PaymentMethod method,

        @NotNull(message = "Payment status is required")
        PaymentStatus status,

        @Size(max = 255, message = "Remarks must not exceed 255 characters")
        String remarks,

        @NotNull(message = "Student is required")
        Long studentId,

        @NotNull(message = "Fee item is required")
        Long feeStructureId
) {
}
