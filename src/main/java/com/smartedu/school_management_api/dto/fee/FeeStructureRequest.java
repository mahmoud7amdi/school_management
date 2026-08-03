package com.smartedu.school_management_api.dto.fee;

import com.smartedu.school_management_api.entity.FeeType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/** The school is derived from the grade, so it is not part of the payload. */
public record FeeStructureRequest(
        @NotBlank(message = "Fee name is required")
        @Size(max = 150, message = "Fee name must not exceed 150 characters")
        String name,

        @NotNull(message = "Fee type is required")
        FeeType feeType,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        @Digits(integer = 10, fraction = 2, message = "Amount is out of range")
        BigDecimal amount,

        @NotNull(message = "Due date is required")
        LocalDate dueDate,

        @Size(max = 255, message = "Description must not exceed 255 characters")
        String description,

        @NotNull(message = "Grade is required")
        Long gradeId,

        @NotNull(message = "Academic year is required")
        Long academicYearId
) {
}
