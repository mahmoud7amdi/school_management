package com.smartedu.school_management_api.controller;

import com.smartedu.school_management_api.dto.ApiResponse;
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
import com.smartedu.school_management_api.service.PortalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * The self-service portal for teachers, students and parents.
 *
 * <p>Separate from the admin controllers because the question is different: these routes
 * return the caller's <em>own</em> records, and none of them accepts a school or a scope
 * parameter. Each method is gated to the roles it serves, and the service re-checks the
 * specific row — the annotation says who may ask, the service says what they may see.
 */
@RestController
@RequestMapping("/api/v1/portal")
@RequiredArgsConstructor
public class PortalController {

    private final PortalService portalService;

    /** The dashboard payload, shaped for whichever portal role is signed in. */
    @GetMapping("/summary")
    @PreAuthorize("hasAnyAuthority('TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<ApiResponse<PortalSummaryResponse>> getSummary() {
        return ResponseEntity.ok(ApiResponse.ok(portalService.getSummary(), "Dashboard loaded"));
    }

    // --- teacher ------------------------------------------------------------

    @GetMapping("/teacher/classes")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<ApiResponse<List<PortalClassResponse>>> getMyClasses() {
        return ResponseEntity.ok(ApiResponse.ok(portalService.getMyClasses(), "Classes loaded"));
    }

    @GetMapping("/teacher/classes/{classroomId}/roster")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> getClassRoster(@PathVariable Long classroomId) {
        return ResponseEntity.ok(ApiResponse.ok(portalService.getClassRoster(classroomId), "Roster loaded"));
    }

    /** Papers the caller may mark. Feeds the exam picker on the marks grid. */
    @GetMapping("/teacher/exams")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<ApiResponse<List<ExamResponse>>> getMyExams() {
        return ResponseEntity.ok(ApiResponse.ok(portalService.getMyExams(), "Exams loaded"));
    }

    @GetMapping("/teacher/absence-notes")
    @PreAuthorize("hasAuthority('TEACHER')")
    public ResponseEntity<ApiResponse<List<AbsenceNoteResponse>>> getAbsenceNotesForReview() {
        return ResponseEntity.ok(ApiResponse.ok(portalService.getAbsenceNotesForReview(),
                "Absence notes loaded"));
    }

    /**
     * Acknowledge or reject a note. Open to the school admin too, so a note still gets
     * actioned when a class has no teacher assigned. Reviewing a note is school-operational
     * work, so a super admin is not included.
     */
    @PutMapping("/absence-notes/{id}/review")
    @PreAuthorize("hasAnyAuthority('TEACHER', 'SCHOOL_ADMIN')")
    public ResponseEntity<ApiResponse<AbsenceNoteResponse>> reviewAbsenceNote(
            @PathVariable Long id, @Valid @RequestBody AbsenceNoteReviewRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(portalService.reviewAbsenceNote(id, request),
                "Absence note reviewed"));
    }

    // --- parent -------------------------------------------------------------

    @GetMapping("/parent/children")
    @PreAuthorize("hasAuthority('PARENT')")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> getMyChildren() {
        return ResponseEntity.ok(ApiResponse.ok(portalService.getMyChildren(), "Children loaded"));
    }

    // --- shared student views -----------------------------------------------
    // One route per record type rather than one per role: the service resolves whose
    // record it is, so a student's own view and a parent's view of a child stay in step.
    // studentId is required for a parent and ignored for a student.

    @GetMapping("/attendance")
    @PreAuthorize("hasAnyAuthority('STUDENT', 'PARENT', 'TEACHER')")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getAttendance(
            @RequestParam(required = false) Long studentId) {
        return ResponseEntity.ok(ApiResponse.ok(portalService.getAttendance(studentId), "Attendance loaded"));
    }

    @GetMapping("/results")
    @PreAuthorize("hasAnyAuthority('STUDENT', 'PARENT', 'TEACHER')")
    public ResponseEntity<ApiResponse<List<ExamResultResponse>>> getResults(
            @RequestParam(required = false) Long studentId) {
        return ResponseEntity.ok(ApiResponse.ok(portalService.getResults(studentId), "Results loaded"));
    }

    @GetMapping("/fees")
    @PreAuthorize("hasAnyAuthority('STUDENT', 'PARENT')")
    public ResponseEntity<ApiResponse<List<StudentFeeLedgerResponse>>> getFees(
            @RequestParam(required = false) Long studentId) {
        return ResponseEntity.ok(ApiResponse.ok(portalService.getFees(studentId), "Fees loaded"));
    }

    @GetMapping("/absence-notes")
    @PreAuthorize("hasAnyAuthority('STUDENT', 'PARENT')")
    public ResponseEntity<ApiResponse<List<AbsenceNoteResponse>>> getAbsenceNotes(
            @RequestParam(required = false) Long studentId) {
        return ResponseEntity.ok(ApiResponse.ok(portalService.getAbsenceNotes(studentId),
                "Absence notes loaded"));
    }

    // --- self-service writes ------------------------------------------------

    @PostMapping("/absence-notes")
    @PreAuthorize("hasAnyAuthority('STUDENT', 'PARENT')")
    public ResponseEntity<ApiResponse<AbsenceNoteResponse>> submitAbsenceNote(
            @Valid @RequestBody AbsenceNoteRequest request) {
        AbsenceNoteResponse created = portalService.submitAbsenceNote(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(created, "Absence note submitted"));
    }

    /** Records that the family has seen a charge. Idempotent; moves no money. */
    @PostMapping("/fees/{feeStructureId}/acknowledge")
    @PreAuthorize("hasAnyAuthority('STUDENT', 'PARENT')")
    public ResponseEntity<ApiResponse<String>> acknowledgeFee(
            @PathVariable Long feeStructureId,
            @RequestParam(required = false) Long studentId) {
        portalService.acknowledgeFee(feeStructureId, studentId);
        return ResponseEntity.ok(ApiResponse.ok("Acknowledged", "Fee acknowledged"));
    }
}
