package com.smartedu.school_management_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * A record that a family has seen a fee item.
 *
 * <p>Purely an acknowledgement — it moves no money and does not touch
 * {@link FeePayment} or a student's balance. Its only job is to let the office see who has
 * read a charge.
 *
 * <p>Unique on (student, fee item, account) so each guardian acknowledges independently
 * and re-clicking is idempotent rather than an error.
 */
@Entity
@Table(name = "fee_acknowledgements",
        uniqueConstraints = @UniqueConstraint(name = "uk_fee_acknowledgement",
                columnNames = {"student_id", "fee_structure_id", "acknowledged_by_id"}),
        indexes = {
                @Index(name = "idx_fee_ack_school", columnList = "school_id"),
                @Index(name = "idx_fee_ack_student", columnList = "student_id"),
                @Index(name = "idx_fee_ack_structure", columnList = "fee_structure_id")
        })
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FeeAcknowledgement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Acknowledgement time is required")
    @Column(name = "acknowledged_at", nullable = false)
    private LocalDateTime acknowledgedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fee_structure_id", nullable = false)
    private FeeStructure feeStructure;

    /** The portal account that clicked acknowledge — a student or one of their guardians. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "acknowledged_by_id", nullable = false)
    private User acknowledgedBy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;
}
