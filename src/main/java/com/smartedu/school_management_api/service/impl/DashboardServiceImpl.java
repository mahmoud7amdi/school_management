package com.smartedu.school_management_api.service.impl;

import com.smartedu.school_management_api.dto.dashboard.DashboardStatsResponse;
import com.smartedu.school_management_api.entity.AcademicYear;
import com.smartedu.school_management_api.entity.Student;
import com.smartedu.school_management_api.entity.StudentStatus;
import com.smartedu.school_management_api.entity.User;
import com.smartedu.school_management_api.entity.UserRole;
import com.smartedu.school_management_api.repository.AcademicYearRepository;
import com.smartedu.school_management_api.repository.ClassroomRepository;
import com.smartedu.school_management_api.repository.GradeRepository;
import com.smartedu.school_management_api.repository.SchoolRepository;
import com.smartedu.school_management_api.repository.StudentRepository;
import com.smartedu.school_management_api.repository.SubjectRepository;
import com.smartedu.school_management_api.repository.UserRepository;
import com.smartedu.school_management_api.service.DashboardService;
import com.smartedu.school_management_api.service.SchoolAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final GradeRepository gradeRepository;
    private final SubjectRepository subjectRepository;
    private final ClassroomRepository classroomRepository;
    private final AcademicYearRepository academicYearRepository;
    private final SchoolAccessService access;

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats() {
        User currentUser = access.currentUser();
        access.requireAcademicManager(currentUser);

        boolean superAdmin = currentUser.getRole() == UserRole.SUPER_ADMIN;
        Long schoolId = superAdmin ? null : currentUser.schoolIdOrNull();

        if (superAdmin) {
            return buildGlobalStats();
        }
        return buildSchoolStats(schoolId, currentUser);
    }

    private DashboardStatsResponse buildGlobalStats() {
        List<Student> students = studentRepository.findAllByOrderByLastNameAscFirstNameAsc();

        return new DashboardStatsResponse(
                schoolRepository.count(),
                userRepository.countByRole(UserRole.SCHOOL_ADMIN),
                studentRepository.count(),
                studentRepository.countByStatus(StudentStatus.ACTIVE),
                userRepository.countByRole(UserRole.TEACHER),
                gradeRepository.count(),
                subjectRepository.count(),
                classroomRepository.count(),
                academicYearRepository.count(),
                null,
                "All schools",
                studentsByGrade(students),
                studentsByStatus(students));
    }

    private DashboardStatsResponse buildSchoolStats(Long schoolId, User currentUser) {
        if (schoolId == null) {
            // School admin with no school yet: report zeroes rather than fail the page.
            return new DashboardStatsResponse(null, 0, 0, 0, 0, 0, 0, 0, 0, null,
                    "No school assigned", List.of(), List.of());
        }

        List<Student> students = studentRepository.findBySchoolIdOrderByLastNameAscFirstNameAsc(schoolId);

        String currentYear = academicYearRepository.findBySchoolIdOrderByStartDateDesc(schoolId).stream()
                .filter(year -> Boolean.TRUE.equals(year.getCurrent()))
                .findFirst()
                .map(AcademicYear::getName)
                .orElse(null);

        return new DashboardStatsResponse(
                null,
                userRepository.countBySchoolId(schoolId),
                studentRepository.countBySchoolId(schoolId),
                studentRepository.countBySchoolIdAndStatus(schoolId, StudentStatus.ACTIVE),
                userRepository.countBySchoolIdAndRole(schoolId, UserRole.TEACHER),
                gradeRepository.countBySchoolId(schoolId),
                subjectRepository.countBySchoolId(schoolId),
                classroomRepository.countBySchoolId(schoolId),
                academicYearRepository.countBySchoolId(schoolId),
                currentYear,
                currentUser.getSchool() != null ? currentUser.getSchool().getName() : "Your school",
                studentsByGrade(students),
                studentsByStatus(students));
    }

    /**
     * Grouped in memory rather than with a GROUP BY: the student list is already
     * loaded for the totals, and this keeps one query instead of three.
     */
    private List<DashboardStatsResponse.StudentsByGrade> studentsByGrade(List<Student> students) {
        Map<String, Long> counts = students.stream()
                .filter(student -> student.getGrade() != null)
                .collect(Collectors.groupingBy(
                        student -> student.getGrade().getName(),
                        LinkedHashMap::new,
                        Collectors.counting()));

        return counts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new DashboardStatsResponse.StudentsByGrade(entry.getKey(), entry.getValue()))
                .toList();
    }

    private List<DashboardStatsResponse.StudentsByStatus> studentsByStatus(List<Student> students) {
        Map<StudentStatus, Long> counts = students.stream()
                .filter(student -> student.getStatus() != null)
                .collect(Collectors.groupingBy(Student::getStatus, Collectors.counting()));

        return counts.entrySet().stream()
                .sorted(Comparator.comparing(entry -> entry.getKey().ordinal()))
                .map(entry -> new DashboardStatsResponse.StudentsByStatus(
                        entry.getKey().getLabel(), entry.getValue()))
                .toList();
    }
}
