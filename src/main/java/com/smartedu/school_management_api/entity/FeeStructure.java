package com.smartedu.school_management_api.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * A chargeable fee item for one grade in one academic year ("Grade 1 tuition",
 * "transport"), reused by every student in that group.
 *
 * <p>Payments live in {@link FeePayment}; a student's balance and settlement
 * status are derived from the amount and the sum of completed payments. Deleting
 * a structure is blocked while any payment references it, so money history cannot
 * silently vanish.
 */
@Entity
@Table(name = "fee_structures",
        uniqueConstraints = @UniqueConstraint(name = "uk_fee_structure_year_grade_name",
                columnNames = {"academic_year_id", "grade_id", "name"}),
        indexes = {
                @Index(name = "idx_fee_structure_school", columnList = "school_id"),
                @Index(name = "idx_fee_structure_year", columnList = "academic_year_id"),
                @Index(name = "idx_fee_structure_grade", columnList = "grade_id")
        })
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FeeStructure extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Fee name is required")
    @Size(max = 150, message = "Fee name must not exceed 150 characters")
    @Column(nullable = false, length = 150)
    private String name;

    @NotNull(message = "Fee type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "fee_type", nullable = false, length = 20)
    private FeeType feeType;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Amount is out of range")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @NotNull(message = "Due date is required")
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Size(max = 255)
    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grade_id", nullable = false)
    private Grade grade;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @OneToMany(mappedBy = "feeStructure", fetch = FetchType.LAZY)
    @Builder.Default
    private List<FeePayment> payments = new ArrayList<>();
}
