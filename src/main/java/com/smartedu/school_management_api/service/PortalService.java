package com.smartedu.school_management_api.service;

import com.smartedu.school_management_api.dto.attendance.AttendanceResponse;
import com.smartedu.school_management_api.dto.exam.ExamResponse;
import com.smartedu.school_management_api.dto.exam.ExamResultResponse;
import com.smartedu.school_management_api.dto.fee.StudentFeeLedgerResponse;
import com.smartedu.school_management_api.dto.portal.AbsenceNoteRequest;
import com.smartedu.school_management_api.dto.portal.AbsenceNoteResponse;
import com.smartedu.school_management_api.dto.portal.AbsenceNoteReviewRequest;
import com.smartedu.school_management_api.dto.portal.PortalClassResponse;
import com.smartedu.school_management_api.dto.portal.PortalSummaryResponse;
import com.smartedu.school_management_api.dto.student.StudentResponse;

import java.util.List;

/**
 * Reads and writes scoped to the caller's own records, for the {@code TEACHER},
 * {@code STUDENT} and {@code PARENT} roles.
 *
 * <p>Every method derives its scope from the signed-in user through
 * {@link PortalAccessService}; none of them accepts a school or a scope parameter.
 */
public interface PortalService {

    /** The role-shaped dashboard payload. */
    PortalSummaryResponse getSummary();

    // --- teacher ------------------------------------------------------------

    List<PortalClassResponse> getMyClasses();

    List<StudentResponse> getClassRoster(Long classroomId);

    /** Exams the caller may mark, across the classes and grades they teach. */
    List<ExamResponse> getMyExams();

    /** Absence notes for students in the caller's classes. */
    List<AbsenceNoteResponse> getAbsenceNotesForReview();

    AbsenceNoteResponse reviewAbsenceNote(Long id, AbsenceNoteReviewRequest request);

    // --- student and parent -------------------------------------------------

    /** Children linked to the signed-in guardian. */
    List<StudentResponse> getMyChildren();

    /**
     * Attendance for one student. {@code studentId} is required for a parent and ignored
     * for a student, who can only ever read their own.
     */
    List<AttendanceResponse> getAttendance(Long studentId);

    List<ExamResultResponse> getResults(Long studentId);

    List<StudentFeeLedgerResponse> getFees(Long studentId);

    List<AbsenceNoteResponse> getAbsenceNotes(Long studentId);

    AbsenceNoteResponse submitAbsenceNote(AbsenceNoteRequest request);

    /** Records that the caller has seen a fee item. Idempotent. */
    void acknowledgeFee(Long feeStructureId, Long studentId);
}
