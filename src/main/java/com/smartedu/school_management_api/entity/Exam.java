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
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A scheduled assessment for one subject and one class.
 *
 * <p>Marks live in {@link ExamResult}, one row per student, and are cascaded away
 * with the exam — a deleted exam has no meaningful results left behind.
 */
@Entity
@Table(name = "exams",
        indexes = {
                @Index(name = "idx_exam_school", columnList = "school_id"),
                @Index(name = "idx_exam_subject", columnList = "subject_id"),
                @Index(name = "idx_exam_classroom", columnList = "classroom_id"),
                @Index(name = "idx_exam_year", columnList = "academic_year_id"),
                @Index(name = "idx_exam_date", columnList = "exam_date")
        })
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Exam extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Exam title is required")
    @Size(max = 150, message = "Exam title must not exceed 150 characters")
    @Column(nullable = false, length = 150)
    private String title;

    @NotNull(message = "Exam type is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "exam_type", nullable = false, length = 20)
    private ExamType examType;

    @NotNull(message = "Exam date is required")
    @Column(name = "exam_date", nullable = false)
    private LocalDate examDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Min(value = 5, message = "Duration must be at least 5 minutes")
    @Max(value = 600, message = "Duration must not exceed 600 minutes")
    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @NotNull(message = "Maximum marks is required")
    @DecimalMin(value = "1.0", message = "Maximum marks must be at least 1")
    @Digits(integer = 4, fraction = 2, message = "Maximum marks is out of range")
    @Column(name = "max_marks", nullable = false, precision = 6, scale = 2)
    private BigDecimal maxMarks;

    @NotNull(message = "Pass marks is required")
    @DecimalMin(value = "0.0", message = "Pass marks cannot be negative")
    @Digits(integer = 4, fraction = 2, message = "Pass marks is out of range")
    @Column(name = "pass_marks", nullable = false, precision = 6, scale = 2)
    private BigDecimal passMarks;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grade_id", nullable = false)
    private Grade grade;

    /** The class sitting the exam. Optional: a grade-wide paper leaves this null. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id")
    private Classroom classroom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<ExamResult> results = new ArrayList<>();
}
