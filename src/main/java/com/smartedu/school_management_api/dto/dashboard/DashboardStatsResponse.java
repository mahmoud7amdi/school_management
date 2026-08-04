package com.smartedu.school_management_api.dto.dashboard;

import java.util.List;

/**
 * Everything a school admin's dashboard home needs, in one round trip.
 *
 * <p>Always scoped to a single school. There is no cross-school variant: a super admin's
 * dashboard is served by the platform report instead, so no field here spans schools.
 */
public record DashboardStatsResponse(
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
