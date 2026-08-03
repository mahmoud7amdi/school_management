package com.smartedu.school_management_api.mapper;

import com.smartedu.school_management_api.dto.common.ReferenceResponse;
import com.smartedu.school_management_api.dto.fee.FeePaymentResponse;
import com.smartedu.school_management_api.dto.fee.FeeStructureResponse;
import com.smartedu.school_management_api.dto.fee.StudentFeeLedgerResponse;
import com.smartedu.school_management_api.entity.AcademicYear;
import com.smartedu.school_management_api.entity.FeePayment;
import com.smartedu.school_management_api.entity.FeeStructure;
import com.smartedu.school_management_api.entity.Grade;
import com.smartedu.school_management_api.entity.School;
import com.smartedu.school_management_api.entity.SettlementStatus;
import com.smartedu.school_management_api.entity.Student;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Mapping for the fee catalogue, the payments against it and the derived ledger. */
@Component
public class FeeMapper {

    public FeeStructureResponse toResponse(FeeStructure fee, BigDecimal totalCollected, long paymentCount) {
        if (fee == null) {
            return null;
        }
        Grade grade = fee.getGrade();
        AcademicYear year = fee.getAcademicYear();

        return new FeeStructureResponse(
                fee.getId(),
                fee.getName(),
                fee.getFeeType(),
                fee.getFeeType() != null ? fee.getFeeType().getLabel() : null,
                fee.getAmount(),
                fee.getDueDate(),
                fee.getDescription(),
                grade != null ? ReferenceResponse.of(grade.getId(), grade.getName()) : null,
                year != null ? ReferenceResponse.of(year.getId(), year.getName()) : null,
                schoolRef(fee.getSchool()),
                totalCollected != null ? totalCollected : BigDecimal.ZERO,
                paymentCount,
                fee.getCreatedAt(),
                fee.getUpdatedAt());
    }

    public FeePaymentResponse toResponse(FeePayment payment) {
        if (payment == null) {
            return null;
        }
        Student student = payment.getStudent();
        FeeStructure fee = payment.getFeeStructure();

        return new FeePaymentResponse(
                payment.getId(),
                payment.getReceiptNumber(),
                payment.getAmountPaid(),
                payment.getPaymentDate(),
                payment.getMethod(),
                payment.getMethod() != null ? payment.getMethod().getLabel() : null,
                payment.getStatus(),
                payment.getStatus() != null ? payment.getStatus().getLabel() : null,
                payment.getRemarks(),
                student != null ? ReferenceResponse.of(student.getId(), student.getFullName()) : null,
                student != null ? student.getAdmissionNumber() : null,
                fee != null ? ReferenceResponse.of(fee.getId(), fee.getName()) : null,
                schoolRef(payment.getSchool()),
                payment.getCreatedAt(),
                payment.getUpdatedAt());
    }

    /**
     * Builds one ledger line. {@code amountPaid} is the sum of that student's
     * completed payments, computed by the service.
     */
    public StudentFeeLedgerResponse toLedger(Student student, FeeStructure fee, BigDecimal amountPaid) {
        BigDecimal due = fee.getAmount() != null ? fee.getAmount() : BigDecimal.ZERO;
        BigDecimal paid = amountPaid != null ? amountPaid : BigDecimal.ZERO;
        BigDecimal balance = due.subtract(paid);

        SettlementStatus settlement;
        if (balance.signum() <= 0) {
            settlement = SettlementStatus.PAID;
        } else if (paid.signum() > 0) {
            settlement = SettlementStatus.PARTIAL;
        } else {
            settlement = SettlementStatus.UNPAID;
        }

        boolean overdue = settlement != SettlementStatus.PAID
                && fee.getDueDate() != null
                && fee.getDueDate().isBefore(LocalDate.now());

        return new StudentFeeLedgerResponse(
                ReferenceResponse.of(student.getId(), student.getFullName()),
                student.getAdmissionNumber(),
                ReferenceResponse.of(fee.getId(), fee.getName()),
                fee.getFeeType() != null ? fee.getFeeType().getLabel() : null,
                fee.getDueDate(),
                due,
                paid,
                balance,
                settlement,
                settlement.getLabel(),
                overdue);
    }

    private ReferenceResponse schoolRef(School school) {
        return school != null ? ReferenceResponse.of(school.getId(), school.getName()) : null;
    }
}
