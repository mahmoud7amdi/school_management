package com.smartedu.school_management_api;

import com.smartedu.school_management_api.entity.AbsenceNoteStatus;
import com.smartedu.school_management_api.entity.AboutPage;
import com.smartedu.school_management_api.entity.AttendanceStatus;
import com.smartedu.school_management_api.entity.EnrollmentStatus;
import com.smartedu.school_management_api.entity.PaymentStatus;
import com.smartedu.school_management_api.entity.StudentStatus;
import com.smartedu.school_management_api.entity.UserRole;
import com.smartedu.school_management_api.repository.AbsenceNoteRepository;
import com.smartedu.school_management_api.repository.AboutPageRepository;
import com.smartedu.school_management_api.repository.AttendanceRepository;
import com.smartedu.school_management_api.repository.ClassroomRepository;
import com.smartedu.school_management_api.repository.EnrollmentRepository;
import com.smartedu.school_management_api.repository.ExamRepository;
import com.smartedu.school_management_api.repository.ExamResultRepository;
import com.smartedu.school_management_api.repository.FeeAcknowledgementRepository;
import com.smartedu.school_management_api.repository.FeePaymentRepository;
import com.smartedu.school_management_api.repository.FeeStructureRepository;
import com.smartedu.school_management_api.repository.ParentRepository;
import com.smartedu.school_management_api.repository.SectionRepository;
import com.smartedu.school_management_api.repository.StudentGuardianRepository;
import com.smartedu.school_management_api.repository.StudentRepository;
import com.smartedu.school_management_api.repository.TeacherRepository;
import com.smartedu.school_management_api.repository.TeachingAssignmentRepository;
import com.smartedu.school_management_api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Executes one query per repository added for sections, teachers, enrollments,
 * attendance, exams, fees and the role portal.
 *
 * <p>Spring Data resolves derived query names when it builds the repository
 * proxies, so a mistyped method name fails the context rather than the assertion.
 * Actually calling each one goes a step further and proves the generated SQL runs
 * against the real schema — which is what catches a property path that parses but
 * does not map to a column.
 *
 * <p>Shares the {@code test} profile with the other tests, so it runs against the
 * scratch {@code school_test_db} and leaves nothing behind.
 */
@SpringBootTest
@ActiveProfiles("test")
class RepositoryQuerySmokeTest {

    @Autowired
    private SectionRepository sectionRepository;
    @Autowired
    private TeacherRepository teacherRepository;
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    @Autowired
    private AttendanceRepository attendanceRepository;
    @Autowired
    private ExamRepository examRepository;
    @Autowired
    private ExamResultRepository examResultRepository;
    @Autowired
    private FeeStructureRepository feeStructureRepository;
    @Autowired
    private FeePaymentRepository feePaymentRepository;
    @Autowired
    private ParentRepository parentRepository;
    @Autowired
    private StudentGuardianRepository studentGuardianRepository;
    @Autowired
    private TeachingAssignmentRepository teachingAssignmentRepository;
    @Autowired
    private AbsenceNoteRepository absenceNoteRepository;
    @Autowired
    private FeeAcknowledgementRepository feeAcknowledgementRepository;
    @Autowired
    private AboutPageRepository aboutPageRepository;
    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private ClassroomRepository classroomRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void sectionQueriesExecute() {
        assertThat(sectionRepository.findAllByOrderByGradeLevelOrderAscNameAsc()).isEmpty();
        assertThat(sectionRepository.findBySchoolIdOrderByGradeLevelOrderAscNameAsc(1L)).isEmpty();
        assertThat(sectionRepository.findWithRelationsById(1L)).isEmpty();
        assertThat(sectionRepository.countBySectionHeadId(1L)).isZero();
        assertThat(sectionRepository.existsByGradeIdAndNameIgnoreCase(1L, "A")).isFalse();
    }

    @Test
    void teacherQueriesExecute() {
        assertThat(teacherRepository.findAllByOrderByLastNameAscFirstNameAsc()).isEmpty();
        assertThat(teacherRepository.findBySchoolIdOrderByLastNameAscFirstNameAsc(1L)).isEmpty();
        assertThat(teacherRepository.findWithRelationsById(1L)).isEmpty();
        assertThat(teacherRepository.findByUserAccountId(UUID.randomUUID())).isEmpty();
        assertThat(teacherRepository.findAssignable(null)).isEmpty();
        assertThat(teacherRepository.findAssignable(1L)).isEmpty();
        assertThat(teacherRepository.existsBySchoolIdAndEmployeeNumberIgnoreCase(1L, "EMP-1")).isFalse();
    }

    @Test
    void enrollmentQueriesExecute() {
        assertThat(enrollmentRepository.findAllByOrderByAcademicYearStartDateDescStudentLastNameAsc()).isEmpty();
        assertThat(enrollmentRepository
                .findBySchoolIdOrderByAcademicYearStartDateDescStudentLastNameAsc(1L)).isEmpty();
        assertThat(enrollmentRepository.findByStudentIdOrderByAcademicYearStartDateDesc(1L)).isEmpty();
        assertThat(enrollmentRepository.findByAcademicYearIdOrderByStudentLastNameAsc(1L)).isEmpty();
        assertThat(enrollmentRepository.findByClassroomIdAndStatusIn(1L,
                List.of(EnrollmentStatus.ENROLLED, EnrollmentStatus.REPEATING))).isEmpty();
        assertThat(enrollmentRepository.existsByStudentIdAndAcademicYearId(1L, 1L)).isFalse();
    }

    @Test
    void attendanceQueriesExecute() {
        LocalDate today = LocalDate.now();

        assertThat(attendanceRepository.findAllByOrderByAttendanceDateDescStudentLastNameAsc()).isEmpty();
        assertThat(attendanceRepository.findBySchoolIdOrderByAttendanceDateDescStudentLastNameAsc(1L)).isEmpty();
        assertThat(attendanceRepository.findRegister(1L, today)).isEmpty();
        assertThat(attendanceRepository.findByClassroomIdAndAttendanceDate(1L, today)).isEmpty();
        assertThat(attendanceRepository.findByStudentIdAndAttendanceDate(1L, today)).isEmpty();
        assertThat(attendanceRepository.findByStudentIdInAndAttendanceDate(List.of(1L, 2L), today)).isEmpty();
        assertThat(attendanceRepository
                .findByStudentIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(
                        1L, today.minusMonths(1), today)).isEmpty();
        assertThat(attendanceRepository.countByRecordedById(1L)).isZero();
        assertThat(attendanceRepository.countDistinctDatesByClassroomId(1L)).isZero();
    }

    @Test
    void examQueriesExecute() {
        assertThat(examRepository.findAllByOrderByExamDateDescTitleAsc()).isEmpty();
        assertThat(examRepository.findBySchoolIdOrderByExamDateDescTitleAsc(1L)).isEmpty();
        assertThat(examRepository.findWithRelationsById(1L)).isEmpty();

        assertThat(examResultRepository.findAllByOrderByExamExamDateDescStudentLastNameAsc()).isEmpty();
        assertThat(examResultRepository.findByExamIdOrderByStudentLastNameAscStudentFirstNameAsc(1L)).isEmpty();
        assertThat(examResultRepository
                .findByExamIdAndStudentClassroomIdOrderByStudentLastNameAsc(1L, 1L)).isEmpty();
        assertThat(examResultRepository.countByExamId(1L)).isZero();
    }

    @Test
    void feeQueriesExecute() {
        assertThat(feeStructureRepository.findAllByOrderByAcademicYearStartDateDescNameAsc()).isEmpty();
        assertThat(feeStructureRepository.findBySchoolIdOrderByAcademicYearStartDateDescNameAsc(1L)).isEmpty();
        assertThat(feeStructureRepository.findByGradeIdOrderByNameAsc(1L)).isEmpty();

        assertThat(feePaymentRepository.findAllByOrderByPaymentDateDesc()).isEmpty();
        assertThat(feePaymentRepository.findBySchoolIdOrderByPaymentDateDesc(1L)).isEmpty();
        assertThat(feePaymentRepository.findByStudentIdOrderByPaymentDateDesc(1L)).isEmpty();
        assertThat(feePaymentRepository.findByFeeStructureIdAndStatus(1L, PaymentStatus.COMPLETED)).isEmpty();
        assertThat(feePaymentRepository.findByStudentIdAndFeeStructureIdAndStatusOrderByPaymentDateAsc(
                1L, 1L, PaymentStatus.COMPLETED)).isEmpty();
        assertThat(feePaymentRepository.countByFeeStructureId(1L)).isZero();
        assertThat(feePaymentRepository.existsByFeeStructureId(1L)).isFalse();
    }

    // --- role portal ------------------------------------------------------
    // The queries below traverse an association to reach a login
    // (teacher.userAccount.id, parent.userAccount.id). That is the shape the
    // schoolIdOrNull() note on User warns about, so each one is executed here.

    @Test
    void parentQueriesExecute() {
        assertThat(parentRepository.findAllByOrderByLastNameAscFirstNameAsc()).isEmpty();
        assertThat(parentRepository.findBySchoolIdOrderByLastNameAscFirstNameAsc(1L)).isEmpty();
        assertThat(parentRepository.findWithRelationsById(1L)).isEmpty();
        assertThat(parentRepository.findByUserAccountId(UUID.randomUUID())).isEmpty();
        assertThat(parentRepository.existsByUserAccountId(UUID.randomUUID())).isFalse();
        assertThat(parentRepository.existsBySchoolIdAndEmailIgnoreCase(1L, "a@b.com")).isFalse();
        assertThat(parentRepository.countBySchoolId(1L)).isZero();
    }

    @Test
    void studentGuardianQueriesExecute() {
        assertThat(studentGuardianRepository
                .findByParentIdOrderByStudentLastNameAscStudentFirstNameAsc(1L)).isEmpty();
        assertThat(studentGuardianRepository
                .findByStudentIdOrderByPrimaryContactDescParentLastNameAsc(1L)).isEmpty();
        assertThat(studentGuardianRepository.findWithRelationsById(1L)).isEmpty();
        assertThat(studentGuardianRepository.findByParentUserAccountId(UUID.randomUUID())).isEmpty();
        assertThat(studentGuardianRepository
                .findStudentIdsByParentUserAccountId(UUID.randomUUID())).isEmpty();
        assertThat(studentGuardianRepository.existsByParentIdAndStudentId(1L, 1L)).isFalse();
        assertThat(studentGuardianRepository.countByParentId(1L)).isZero();
        assertThat(studentGuardianRepository.countByStudentId(1L)).isZero();
    }

    @Test
    void teachingAssignmentQueriesExecute() {
        assertThat(teachingAssignmentRepository.findAllByOrderByClassroomNameAsc()).isEmpty();
        assertThat(teachingAssignmentRepository.findBySchoolIdOrderByClassroomNameAsc(1L)).isEmpty();
        assertThat(teachingAssignmentRepository.findWithRelationsById(1L)).isEmpty();
        assertThat(teachingAssignmentRepository.findByTeacherIdOrderByClassroomNameAsc(1L)).isEmpty();
        assertThat(teachingAssignmentRepository.findByClassroomIdOrderBySubjectNameAsc(1L)).isEmpty();
        assertThat(teachingAssignmentRepository.findByTeacherUserAccountId(UUID.randomUUID())).isEmpty();
        assertThat(teachingAssignmentRepository
                .findClassroomIdsByTeacherUserAccountId(UUID.randomUUID())).isEmpty();
        assertThat(teachingAssignmentRepository.countByTeacherId(1L)).isZero();
        assertThat(teachingAssignmentRepository.countByClassroomId(1L)).isZero();

        // Both branches of the nullable-subject predicate: a derived query would emit
        // "subject_id = null" for the null case and silently never match.
        assertThat(teachingAssignmentRepository.existsAssignment(1L, 1L, null, 1L)).isFalse();
        assertThat(teachingAssignmentRepository.existsAssignment(1L, 1L, 1L, 1L)).isFalse();
        assertThat(teachingAssignmentRepository.existsAssignmentExcluding(1L, 1L, null, 1L, 2L)).isFalse();
        assertThat(teachingAssignmentRepository.existsAssignmentExcluding(1L, 1L, 1L, 1L, 2L)).isFalse();
    }

    @Test
    void absenceNoteQueriesExecute() {
        LocalDate today = LocalDate.now();

        assertThat(absenceNoteRepository.findAllByOrderByAbsenceDateDesc()).isEmpty();
        assertThat(absenceNoteRepository.findBySchoolIdOrderByAbsenceDateDesc(1L)).isEmpty();
        assertThat(absenceNoteRepository.findWithRelationsById(1L)).isEmpty();
        assertThat(absenceNoteRepository.findByStudentIdOrderByAbsenceDateDesc(1L)).isEmpty();
        assertThat(absenceNoteRepository.findBySchoolIdAndStatusOrderByAbsenceDateDesc(
                1L, AbsenceNoteStatus.SUBMITTED)).isEmpty();
        assertThat(absenceNoteRepository
                .findByStudentClassroomIdInOrderByAbsenceDateDesc(List.of(1L, 2L))).isEmpty();
        assertThat(absenceNoteRepository.findByStudentIdInOrderByAbsenceDateDesc(List.of(1L, 2L))).isEmpty();
        assertThat(absenceNoteRepository.existsByStudentIdAndAbsenceDate(1L, today)).isFalse();
        assertThat(absenceNoteRepository.findByStudentIdAndAbsenceDate(1L, today)).isEmpty();
        assertThat(absenceNoteRepository.countBySchoolIdAndStatus(1L, AbsenceNoteStatus.SUBMITTED)).isZero();
        assertThat(absenceNoteRepository.countByStudentIdAndStatus(1L, AbsenceNoteStatus.SUBMITTED)).isZero();
    }

    @Test
    void feeAcknowledgementQueriesExecute() {
        assertThat(feeAcknowledgementRepository.findByStudentIdOrderByAcknowledgedAtDesc(1L)).isEmpty();
        assertThat(feeAcknowledgementRepository.findWithRelationsById(1L)).isEmpty();
        assertThat(feeAcknowledgementRepository
                .existsByStudentIdAndFeeStructureIdAndAcknowledgedById(1L, 1L, UUID.randomUUID())).isFalse();
        assertThat(feeAcknowledgementRepository
                .findByStudentIdAndFeeStructureIdAndAcknowledgedById(1L, 1L, UUID.randomUUID())).isEmpty();
        assertThat(feeAcknowledgementRepository.existsByStudentIdAndFeeStructureId(1L, 1L)).isFalse();
        assertThat(feeAcknowledgementRepository.countByFeeStructureId(1L)).isZero();
    }

    @Test
    void portalLookupsOnExistingRepositoriesExecute() {
        assertThat(studentRepository.findByUserAccountId(UUID.randomUUID())).isEmpty();
        assertThat(studentRepository.findByClassroomIdInOrderByLastNameAscFirstNameAsc(List.of(1L, 2L))).isEmpty();
        assertThat(studentRepository.findIdsByClassroomIdIn(List.of(1L, 2L))).isEmpty();
        assertThat(classroomRepository.findByIdInOrderByNameAsc(List.of(1L, 2L))).isEmpty();
        assertThat(classroomRepository.findIdsByClassTeacherId(UUID.randomUUID())).isEmpty();

        assertThat(attendanceRepository.findByStudentIdOrderByAttendanceDateDesc(1L)).isEmpty();
        assertThat(attendanceRepository.countByStudentId(1L)).isZero();
        assertThat(attendanceRepository.countByStudentIdAndStatus(1L, AttendanceStatus.PRESENT)).isZero();

        assertThat(examResultRepository.findByStudentIdOrderByExamExamDateDesc(1L)).isEmpty();
        assertThat(examResultRepository.countByStudentId(1L)).isZero();

        assertThat(userRepository.searchAdmins(null, null)).isNotNull();
        // A school role filter matches no administrator, so this narrows to nothing
        // rather than widening past the two admin tiers the query is limited to.
        assertThat(userRepository.searchAdmins(1L, UserRole.TEACHER)).isEmpty();
        assertThat(userRepository.searchSchoolMembers(1L, null)).isEmpty();
        assertThat(userRepository.searchSchoolMembers(1L, UserRole.PARENT)).isEmpty();
    }

    /**
     * The grouped counts behind the platform report. These are hand-written JPQL with
     * an aliased projection, so running each one proves the aliases match
     * {@link com.smartedu.school_management_api.repository.SchoolCountProjection}'s
     * getters — a mismatch there fails at query time, not at context startup.
     */
    @Test
    void platformReportGroupedCountsExecute() {
        assertThat(userRepository.countByRoleGroupedBySchool(UserRole.SCHOOL_ADMIN)).isNotNull();
        assertThat(teacherRepository.countGroupedBySchool()).isNotNull();
        assertThat(studentRepository.countGroupedBySchool()).isNotNull();
        assertThat(studentRepository.countByStatusGroupedBySchool(StudentStatus.ACTIVE)).isNotNull();
    }

    /**
     * The About page row. Proves the entity maps to a real table — its TEXT columns and
     * the fixed singleton id are new, so a mapping error would otherwise only surface
     * the first time somebody opened the public page.
     */
    @Test
    void aboutPageLookupExecutes() {
        assertThat(aboutPageRepository.findById(AboutPage.SINGLETON_ID)).isNotNull();
        assertThat(aboutPageRepository.count()).isNotNegative();
    }
}
