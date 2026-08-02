package com.smartedu.school_management_api.dto.dashboard;

import java.util.List;

/**
 * Everything the dashboard home needs, in one round trip.
 *
 * <p>{@code schools} is null for a school admin, whose view is scoped to one school.
 */
public record DashboardStatsResponse(
        Long schools,
        long users,
        long students,
        long activeStudents,
        long teachers,
        long grades,
        long subjects,
        long classrooms,
        long academicYears,
        String currentAcademicYear,
        String scopeLabel,
        List<StudentsByGrade> studentsByGrade,
        List<StudentsByStatus> studentsByStatus
) {

    public record StudentsByGrade(String grade, long count) {
    }

    public record StudentsByStatus(String status, long count) {
    }
}
