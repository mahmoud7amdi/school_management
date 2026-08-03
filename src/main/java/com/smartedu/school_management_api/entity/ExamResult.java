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
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * One student's marks for one exam.
 *
 * <p>Percentage, pass/fail and the letter grade are all derived from
 * {@code marksObtained} against the parent exam rather than stored, so they cannot
 * drift if an exam's maximum is later corrected.
 */
@Entity
@Table(name = "exam_results",
        uniqueConstraints = @UniqueConstraint(name = "uk_exam_result_exam_student",
                columnNames = {"exam_id", "student_id"}),
        indexes = {
                @Index(name = "idx_exam_result_school", columnList = "school_id"),
                @Index(name = "idx_exam_result_exam", columnList = "exam_id"),
                @Index(name = "idx_exam_result_student", columnList = "student_id")
        })
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExamResult extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Null means the student has not been marked yet — distinct from a zero. */
    @DecimalMin(value = "0.0", message = "Marks cannot be negative")
    @Digits(integer = 4, fraction = 2, message = "Marks value is out of range")
    @Column(name = "marks_obtained", precision = 6, scale = 2)
    private BigDecimal marksObtained;

    /** True when the student did not sit the paper; marks stay null. */
    @Column(name = "absent", nullable = false)
    @Builder.Default
    private Boolean absent = false;

    @Size(max = 255, message = "Remarks must not exceed 255 characters")
    @Column(name = "remarks")
    private String remarks;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    /** Marks as a percentage of the exam maximum, or null if unmarked. */
    @Transient
    public BigDecimal getPercentage() {
        if (marksObtained == null || exam == null || exam.getMaxMarks() == null
                || exam.getMaxMarks().signum() == 0) {
            return null;
        }
        return marksObtained
                .multiply(BigDecimal.valueOf(100))
                .divide(exam.getMaxMarks(), 2, RoundingMode.HALF_UP);
    }

    /** Null while unmarked, so the UI can distinguish "not yet marked" from "failed". */
    @Transient
    public Boolean getPassed() {
        if (Boolean.TRUE.equals(absent)) {
            return false;
        }
        if (marksObtained == null || exam == null || exam.getPassMarks() == null) {
            return null;
        }
        return marksObtained.compareTo(exam.getPassMarks()) >= 0;
    }

    /**
     * Letter grade on a conventional scale. Derived from the percentage so it stays
     * consistent across exams with different maximums.
     */
    @Transient
    public String getGradeLetter() {
        if (Boolean.TRUE.equals(absent)) {
            return "ABS";
        }
        BigDecimal percentage = getPercentage();
        if (percentage == null) {
            return null;
        }
        double value = percentage.doubleValue();
        if (value >= 90) {
            return "A+";
        }
        if (value >= 80) {
            return "A";
        }
        if (value >= 70) {
            return "B";
        }
        if (value >= 60) {
            return "C";
        }
        if (value >= 50) {
            return "D";
        }
        return "F";
    }
}
