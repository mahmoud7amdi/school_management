package com.smartedu.school_management_api.repository;

import com.smartedu.school_management_api.entity.FeeAcknowledgement;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeeAcknowledgementRepository extends JpaRepository<FeeAcknowledgement, Long> {

    @EntityGraph(attributePaths = {"school", "student", "feeStructure", "acknowledgedBy"})
    List<FeeAcknowledgement> findByStudentIdOrderByAcknowledgedAtDesc(Long studentId);

    @EntityGraph(attributePaths = {"school", "student", "feeStructure", "acknowledgedBy"})
    Optional<FeeAcknowledgement> findWithRelationsById(Long id);

    /** Idempotency check for the acknowledge endpoint. */
    boolean existsByStudentIdAndFeeStructureIdAndAcknowledgedById(Long studentId,
                                                                 Long feeStructureId,
                                                                 UUID acknowledgedById);

    Optional<FeeAcknowledgement> findByStudentIdAndFeeStructureIdAndAcknowledgedById(Long studentId,
                                                                                    Long feeStructureId,
                                                                                    UUID acknowledgedById);

    /** Whether anyone has acknowledged a fee item for a student, for the office view. */
    boolean existsByStudentIdAndFeeStructureId(Long studentId, Long feeStructureId);

    long countByFeeStructureId(Long feeStructureId);
}
