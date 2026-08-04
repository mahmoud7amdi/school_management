package com.smartedu.school_management_api.service.impl;

import com.smartedu.school_management_api.dto.report.PlatformReportResponse;
import com.smartedu.school_management_api.entity.School;
import com.smartedu.school_management_api.entity.StudentStatus;
import com.smartedu.school_management_api.entity.User;
import com.smartedu.school_management_api.entity.UserRole;
import com.smartedu.school_management_api.repository.SchoolCountProjection;
import com.smartedu.school_management_api.repository.SchoolRepository;
import com.smartedu.school_management_api.repository.StudentRepository;
import com.smartedu.school_management_api.repository.TeacherRepository;
import com.smartedu.school_management_api.repository.UserRepository;
import com.smartedu.school_management_api.service.ReportService;
import com.smartedu.school_management_api.service.SchoolAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The platform report.
 *
 * <p>Aggregates only. This is the one place a super admin sees figures drawn from inside
 * schools, so it reports counts per school and never a named student, guardian or staff
 * member — oversight without the roster.
 *
 * <p>Every per-school figure comes from a {@code group by} rather than a query per school,
 * so the whole report is a fixed six queries regardless of how many schools exist.
 */
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private static final DateTimeFormatter MEMBER_SINCE = DateTimeFormatter.ofPattern("d MMM yyyy");

    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final SchoolAccessService access;

    @Override
    @Transactional(readOnly = true)
    public PlatformReportResponse getPlatformReport() {
        User currentUser = access.currentUser();
        access.requirePlatformAdmin(currentUser);

        List<School> schools = schoolRepository.findAllByOrderByNameAsc();

        Map<Long, Long> admins = toMap(userRepository.countByRoleGroupedBySchool(UserRole.SCHOOL_ADMIN));
        Map<Long, Long> teachers = toMap(teacherRepository.countGroupedBySchool());
        Map<Long, Long> students = toMap(studentRepository.countGroupedBySchool());
        Map<Long, Long> activeStudents =
                toMap(studentRepository.countByStatusGroupedBySchool(StudentStatus.ACTIVE));

        List<PlatformReportResponse.SchoolReportRow> rows = schools.stream()
                .map(school -> new PlatformReportResponse.SchoolReportRow(
                        school.getId(),
                        school.getName(),
                        school.getEmail(),
                        count(admins, school.getId()),
                        count(teachers, school.getId()),
                        count(students, school.getId()),
                        count(activeStudents, school.getId()),
                        !Boolean.FALSE.equals(school.getActive())))
                .toList();

        return new PlatformReportResponse(profileOf(currentUser), totalsOf(rows), rows);
    }

    private PlatformReportResponse.AdminProfile profileOf(User user) {
        return new PlatformReportResponse.AdminProfile(
                user.getFullName(),
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole() != null ? user.getRole().getLabel() : null,
                user.getCreatedAt() != null ? MEMBER_SINCE.format(user.getCreatedAt()) : null);
    }

    /**
     * Totals derived from the rows already built, rather than re-queried. The per-school
     * figures are the same numbers, so summing them keeps the two halves of the report
     * consistent even if a school is added mid-request.
     */
    private PlatformReportResponse.PlatformTotals totalsOf(
            List<PlatformReportResponse.SchoolReportRow> rows) {

        long active = rows.stream().filter(PlatformReportResponse.SchoolReportRow::active).count();
        long withoutAdmin = rows.stream()
                .filter(row -> row.admins() == 0)
                .count();

        return new PlatformReportResponse.PlatformTotals(
                rows.size(),
                active,
                rows.size() - active,
                userRepository.countByRole(UserRole.SUPER_ADMIN),
                rows.stream().mapToLong(PlatformReportResponse.SchoolReportRow::admins).sum(),
                withoutAdmin,
                userRepository.count(),
                rows.stream().mapToLong(PlatformReportResponse.SchoolReportRow::students).sum(),
                rows.stream().mapToLong(PlatformReportResponse.SchoolReportRow::teachers).sum());
    }

    private static Map<Long, Long> toMap(List<SchoolCountProjection> counts) {
        Map<Long, Long> map = new HashMap<>();
        for (SchoolCountProjection count : counts) {
            // A null school id means orphaned rows, which belong to no school's figures.
            if (count.getSchoolId() != null) {
                map.put(count.getSchoolId(), count.getTotal());
            }
        }
        return map;
    }

    private static long count(Map<Long, Long> counts, Long schoolId) {
        return counts.getOrDefault(schoolId, 0L);
    }
}
