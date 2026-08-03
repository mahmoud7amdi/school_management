package com.smartedu.school_management_api.dto.portal;

import com.smartedu.school_management_api.entity.UserRole;

import java.util.List;

/**
 * The dashboard payload for a portal role, in one round trip.
 *
 * <p>Role-shaped: exactly one of {@code teacher}, {@code student} or {@code parent} is
 * populated and the rest are null, which Jackson drops (the app sets
 * {@code default-property-inclusion: non_null}). Kept separate from
 * {@code DashboardStatsResponse} because that one answers an admin's question — counts
 * across a school — while this one answers "what is mine".
 */
public record PortalSummaryResponse(
        UserRole role,
        String roleLabel,
        String displayName,
        String schoolName,
        TeacherSummary teacher,
        StudentSummary student,
        ParentSummary parent
) {

    public record TeacherSummary(
            long classes,
            long students,
            long subjects,
            long pendingAbsenceNotes,
            /** Null when the staff record carries no employee number yet. */
            String employeeNumber,
            List<ClassSummary> classList
    ) {
    }

    public record StudentSummary(
            String admissionNumber,
            String gradeName,
            String classroomName,
            long attendanceRecorded,
            long daysPresent,
            long daysAbsent,
            /** Whole percent, or null when no register has been taken yet. */
            Integer attendanceRate,
            long resultsPublished,
            long examsPassed,
            long outstandingFeeItems,
            long submittedAbsenceNotes
    ) {
    }

    public record ParentSummary(
            long children,
            List<ChildSummary> childList
    ) {
    }

    /** One class in a teacher's list, with the counts the tile shows. */
    public record ClassSummary(
            Long classroomId,
            String classroomName,
            String gradeName,
            String subjectNames,
            long students
    ) {
    }

    /** One child on a parent's dashboard. */
    public record ChildSummary(
            Long studentId,
            String studentName,
            String admissionNumber,
            String gradeName,
            String classroomName,
            Integer attendanceRate,
            long outstandingFeeItems
    ) {
    }
}
