package com.smartedu.school_management_api.entity;

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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * An explanation for one day's absence, submitted from the student or parent portal.
 *
 * <p>Deliberately separate from {@link Attendance}: the register is the school's record of
 * what happened, authored by staff, whereas this is the family's account of why. Keeping
 * them apart means a note can be submitted before, after, or without a matching register
 * row, and a rejected note never silently rewrites attendance.
 *
 * <p>Unique on (student, date) so one day carries at most one note.
 */
@Entity
@Table(name = "absence_notes",
        uniqueConstraints = @UniqueConstraint(name = "uk_absence_note_student_date",
                columnNames = {"student_id", "absence_date"}),
        indexes = {
                @Index(name = "idx_absence_note_school", columnList = "school_id"),
                @Index(name = "idx_absence_note_student", columnList = "student_id"),
                @Index(name = "idx_absence_note_status", columnList = "status"),
                @Index(name = "idx_absence_note_date", columnList = "absence_date")
        })
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AbsenceNote extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Absence date is required")
    @Column(name = "absence_date", nullable = false)
    private LocalDate absenceDate;

    @NotBlank(message = "Please give a reason for the absence")
    @Size(max = 1000, message = "Reason must not exceed 1000 characters")
    @Column(name = "reason", nullable = false, length = 1000)
    private String reason;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private AbsenceNoteStatus status = AbsenceNoteStatus.SUBMITTED;

    /** The account that filed the note — the student themselves, or one of their guardians. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submitted_by_id", nullable = false)
    private User submittedBy;

    /** The teacher or admin who actioned it. Null while the note is still SUBMITTED. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Size(max = 500, message = "Review note must not exceed 500 characters")
    @Column(name = "review_note", length = 500)
    private String reviewNote;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;
}
