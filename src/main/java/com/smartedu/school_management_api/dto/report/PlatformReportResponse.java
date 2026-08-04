package com.smartedu.school_management_api.dto.report;

import java.util.List;

/**
 * The super admin's read-only view of the platform.
 *
 * <p>Reporting is the one place a super admin sees figures from inside schools, and it is
 * deliberately aggregate-only: counts per school, never a named student, guardian or
 * staff member. That keeps the role's oversight duty satisfied without handing it the
 * roster it is not responsible for.
 *
 * @param profile   the signed-in super admin's own details
 * @param totals    platform-wide figures
 * @param schools   one row per school, for the breakdown table
 */
public record PlatformReportResponse(
        AdminProfile profile,
        PlatformTotals totals,
        List<SchoolReportRow> schools
) {

    /** Identity of the signed-in administrator, for the dashboard's personal card. */
    public record AdminProfile(
            String fullName,
            String username,
            String email,
            String phoneNumber,
            String roleLabel,
            String memberSince
    ) {
    }

    public record PlatformTotals(
            long schools,
            long activeSchools,
            long inactiveSchools,
            long superAdmins,
            long schoolAdmins,
            long schoolsWithoutAdmin,
            long users,
            long students,
            long teachers
    ) {
    }

    /**
     * @param admins   school admins appointed to this school; 0 flags a gap to fill
     * @param students enrolled students, as a size signal only
     */
    public record SchoolReportRow(
            Long id,
            String name,
            String email,
            long admins,
            long teachers,
            long students,
            long activeStudents,
            boolean active
    ) {
    }
}
