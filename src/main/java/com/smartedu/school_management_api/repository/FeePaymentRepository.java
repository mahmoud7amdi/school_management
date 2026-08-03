package com.smartedu.school_management_api.repository;

import com.smartedu.school_management_api.entity.FeePayment;
import com.smartedu.school_management_api.entity.PaymentStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeePaymentRepository extends JpaRepository<FeePayment, Long> {

    @EntityGraph(attributePaths = {"student", "feeStructure", "school"})
    List<FeePayment> findAllByOrderByPaymentDateDesc();

    @EntityGraph(attributePaths = {"student", "feeStructure", "school"})
    List<FeePayment> findBySchoolIdOrderByPaymentDateDesc(Long schoolId);

    @EntityGraph(attributePaths = {"student", "feeStructure", "school"})
    Optional<FeePayment> findWithRelationsById(Long id);

    boolean existsBySchoolIdAndReceiptNumberIgnoreCase(Long schoolId, String receiptNumber);

    boolean existsBySchoolIdAndReceiptNumberIgnoreCaseAndIdNot(Long schoolId, String receiptNumber, Long id);

    /** Completed payments only — pending/failed rows do not reduce the balance. */
    @EntityGraph(attributePaths = {"student", "feeStructure", "school"})
    List<FeePayment> findByStudentIdAndFeeStructureIdAndStatusOrderByPaymentDateAsc(
            Long studentId, Long feeStructureId, PaymentStatus status);

    /** Completed payments for one fee item, across all students. */
    @EntityGraph(attributePaths = {"student", "feeStructure", "school"})
    List<FeePayment> findByFeeStructureIdAndStatus(Long feeStructureId, PaymentStatus status);

    @EntityGraph(attributePaths = {"student", "feeStructure", "school"})
    List<FeePayment> findByStudentIdOrderByPaymentDateDesc(Long studentId);

    long countBySchoolId(Long schoolId);

    boolean existsByFeeStructureId(Long feeStructureId);

    long countByFeeStructureId(Long feeStructureId);
}
