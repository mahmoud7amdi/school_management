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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * A concrete class group: one grade, in one academic year, at one school.
 * Names are unique within that triple, so "1-A" can exist in successive years.
 */
@Entity
@Table(name = "classrooms",
        uniqueConstraints = @UniqueConstraint(name = "uk_classroom_grade_year_name",
                columnNames = {"grade_id", "academic_year_id", "name"}),
        indexes = {
                @Index(name = "idx_classroom_school", columnList = "school_id"),
                @Index(name = "idx_classroom_grade", columnList = "grade_id"),
                @Index(name = "idx_classroom_year", columnList = "academic_year_id")
        })
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Classroom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Classroom name is required")
    @Size(max = 80, message = "Classroom name must not exceed 80 characters")
    @Column(nullable = false, length = 80)
    private String name;

    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 500, message = "Capacity must not exceed 500")
    @Column(name = "capacity")
    private Integer capacity;

    @Size(max = 50)
    @Column(name = "room_number", length = 50)
    private String roomNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grade_id", nullable = false)
    private Grade grade;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    /** Homeroom teacher. Optional, and cleared rather than cascaded if the user goes away. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_teacher_id")
    private User classTeacher;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    /** Students are unassigned (not deleted) when a classroom is removed. */
    @OneToMany(mappedBy = "classroom", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Student> students = new ArrayList<>();
}
