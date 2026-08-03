package com.smartedu.school_management_api.entity;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * What a teacher actually teaches: one subject, in one class, for one academic year.
 *
 * <p>This is the authority for a teacher's scope. Without it the only link between staff
 * and a class is {@link Classroom#getClassTeacher()}, which answers a narrower question
 * ("who is the homeroom teacher") and cannot express a subject teacher who visits several
 * classes.
 *
 * <p>Points at {@link Teacher}, the HR record, rather than {@link User} — consistent with
 * {@link Section#getSectionHead()} and {@link Attendance#getRecordedBy()}. The bridge to a
 * login is {@link Teacher#getUserAccount()}.
 *
 * <p>{@code subject} is optional: a null means the assignment covers the whole class rather
 * than one subject, which is how a homeroom slot is recorded. Note that MySQL treats NULLs
 * as distinct in a unique index, so the constraint below does not prevent two whole-class
 * rows for the same teacher and class; the service checks that case explicitly.
 */
@Entity
@Table(name = "teaching_assignments",
        uniqueConstraints = @UniqueConstraint(name = "uk_teaching_assignment",
                columnNames = {"teacher_id", "classroom_id", "subject_id", "academic_year_id"}),
        indexes = {
                @Index(name = "idx_teaching_assignment_school", columnList = "school_id"),
                @Index(name = "idx_teaching_assignment_teacher", columnList = "teacher_id"),
                @Index(name = "idx_teaching_assignment_classroom", columnList = "classroom_id"),
                @Index(name = "idx_teaching_assignment_year", columnList = "academic_year_id")
        })
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeachingAssignment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    /** Null means the assignment covers the whole class rather than a single subject. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "academic_year_id", nullable = false)
    private AcademicYear academicYear;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;
}
