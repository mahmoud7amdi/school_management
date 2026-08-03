package com.smartedu.school_management_api.service.impl;

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
import com.smartedu.school_management_api.entity.AbsenceNote;
import com.smartedu.school_management_api.entity.AbsenceNoteStatus;
import com.smartedu.school_management_api.entity.Attendance;
import com.smartedu.school_management_api.entity.AttendanceStatus;
import com.smartedu.school_management_api.entity.Classroom;
import com.smartedu.school_management_api.entity.Exam;
import com.smartedu.school_management_api.entity.FeeAcknowledgement;
import com.smartedu.school_management_api.entity.FeeStructure;
import com.smartedu.school_management_api.entity.Parent;
import com.smartedu.school_management_api.entity.PaymentStatus;
import com.smartedu.school_management_api.entity.Student;
import com.smartedu.school_management_api.entity.StudentGuardian;
import com.smartedu.school_management_api.entity.Subject;
import com.smartedu.school_management_api.entity.Teacher;
import com.smartedu.school_management_api.entity.TeachingAssignment;
import com.smartedu.school_management_api.entity.User;
import com.smartedu.school_management_api.entity.UserRole;
import com.smartedu.school_management_api.exception.AccessDeniedAppException;
import com.smartedu.school_management_api.exception.BadRequestException;
import com.smartedu.school_management_api.exception.DuplicateResourceException;
import com.smartedu.school_management_api.exception.NotFoundException;
import com.smartedu.school_management_api.mapper.ExamMapper;
import com.smartedu.school_management_api.mapper.FeeMapper;
import com.smartedu.school_management_api.mapper.EnrollmentMapper;
import com.smartedu.school_management_api.mapper.PortalMapper;
import com.smartedu.school_management_api.mapper.StudentMapper;
import com.smartedu.school_management_api.repository.AbsenceNoteRepository;
import com.smartedu.school_management_api.repository.AttendanceRepository;
import com.smartedu.school_management_api.repository.ClassroomRepository;
import com.smartedu.school_management_api.repository.ExamRepository;
import com.smartedu.school_management_api.repository.ExamResultRepository;
import com.smartedu.school_management_api.repository.FeeAcknowledgementRepository;
import com.smartedu.school_management_api.repository.FeePaymentRepository;
import com.smartedu.school_management_api.repository.FeeStructureRepository;
import com.smartedu.school_management_api.repository.StudentGuardianRepository;
import com.smartedu.school_management_api.repository.StudentRepository;
import com.smartedu.school_management_api.repository.TeachingAssignmentRepository;
import com.smartedu.school_management_api.service.PortalAccessService;
import com.smartedu.school_management_api.service.PortalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The self-service portal.
 *
 * <p>Every method resolves its own scope through {@link PortalAccessService} and never
 * touches {@code SchoolAccessService}, whose entry points deny these roles. Reads reuse
 * the existing mappers, so a mark or a fee line looks the same here as it does on the
 * admin screens.
 */
@Service
@RequiredArgsConstructor
public class PortalServiceImpl implements PortalService {

    private final PortalAccessService portalAccess;
    private final ClassroomRepository classroomRepository;
    private final StudentRepository studentRepository;
    private final StudentGuardianRepository studentGuardianRepository;
    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final AttendanceRepository attendanceRepository;
    private final ExamRepository examRepository;
    private final ExamResultRepository examResultRepository;
    private final FeeStructureRepository feeStructureRepository;
    private final FeePaymentRepository feePaymentRepository;
    private final FeeAcknowledgementRepository feeAcknowledgementRepository;
    private final AbsenceNoteRepository absenceNoteRepository;
    private final StudentMapper studentMapper;
    private final EnrollmentMapper enrollmentMapper;
    private final ExamMapper examMapper;
    private final FeeMapper feeMapper;
    private final PortalMapper portalMapper;

    // ---------------------------------------------------------------- summary

    @Override
    @Transactional(readOnly = true)
    public PortalSummaryResponse getSummary() {
        User user = portalAccess.currentUser();
        String schoolName = user.getSchool() != null ? user.getSchool().getName() : null;

        return switch (user.getRole()) {
            case TEACHER -> new PortalSummaryResponse(user.getRole(), user.getRole().getLabel(),
                    user.getFullName(), schoolName, teacherSummary(), null, null);
            case STUDENT -> new PortalSummaryResponse(user.getRole(), user.getRole().getLabel(),
                    user.getFullName(), schoolName, null, studentSummary(portalAccess.currentStudent()), null);
            case PARENT -> new PortalSummaryResponse(user.getRole(), user.getRole().getLabel(),
                    user.getFullName(), schoolName, null, null, parentSummary());
            default -> throw new AccessDeniedAppException(
                    "This view is only available to teachers, students and parents");
        };
    }

    private PortalSummaryResponse.TeacherSummary teacherSummary() {
        List<PortalClassResponse> classes = getMyClasses();
        Teacher teacher = portalAccess.currentTeacherOrEmpty().orElse(null);

        long students = classes.stream().mapToLong(PortalClassResponse::students).sum();
        long subjects = classes.stream()
                .flatMap(c -> c.subjects().stream())
                .map(ref -> ref.id())
                .distinct()
                .count();

        List<Long> classroomIds = classes.stream().map(PortalClassResponse::id).toList();
        long pending = classroomIds.isEmpty() ? 0L
                : absenceNoteRepository.findByStudentClassroomIdInOrderByAbsenceDateDesc(classroomIds).stream()
                .filter(note -> note.getStatus() == AbsenceNoteStatus.SUBMITTED)
                .count();

        List<PortalSummaryResponse.ClassSummary> classList = classes.stream()
                .map(c -> new PortalSummaryResponse.ClassSummary(
                        c.id(),
                        c.name(),
                        c.grade() != null ? c.grade().name() : null,
                        c.subjects().isEmpty()
                                ? "Whole class"
                                : String.join(", ", c.subjects().stream().map(ref -> ref.name()).toList()),
                        c.students()))
                .toList();

        return new PortalSummaryResponse.TeacherSummary(
                classes.size(), students, subjects, pending,
                teacher != null ? teacher.getEmployeeNumber() : null,
                classList);
    }

    private PortalSummaryResponse.StudentSummary studentSummary(Student student) {
        long recorded = attendanceRepository.countByStudentId(student.getId());
        long present = attendanceRepository.countByStudentIdAndStatus(student.getId(), AttendanceStatus.PRESENT);
        long absent = attendanceRepository.countByStudentIdAndStatus(student.getId(), AttendanceStatus.ABSENT);

        List<ExamResultResponse> results = examResultRepository
                .findByStudentIdOrderByExamExamDateDesc(student.getId())
                .stream().map(examMapper::toResponse).toList();
        long passed = results.stream().filter(r -> Boolean.TRUE.equals(r.passed())).count();

        long outstanding = ledgerFor(student).stream()
                .filter(line -> line.balance().signum() > 0)
                .count();

        return new PortalSummaryResponse.StudentSummary(
                student.getAdmissionNumber(),
                student.getGrade() != null ? student.getGrade().getName() : null,
                student.getClassroom() != null ? student.getClassroom().getName() : null,
                recorded,
                present,
                absent,
                attendanceRate(recorded, present),
                results.size(),
                passed,
                outstanding,
                absenceNoteRepository.countByStudentIdAndStatus(student.getId(), AbsenceNoteStatus.SUBMITTED));
    }

    private PortalSummaryResponse.ParentSummary parentSummary() {
        List<StudentGuardian> links = studentGuardianRepository
                .findByParentUserAccountId(portalAccess.currentUser().getId());

        List<PortalSummaryResponse.ChildSummary> children = links.stream()
                .map(StudentGuardian::getStudent)
                .map(child -> {
                    long recorded = attendanceRepository.countByStudentId(child.getId());
                    long present = attendanceRepository
                            .countByStudentIdAndStatus(child.getId(), AttendanceStatus.PRESENT);
                    long outstanding = ledgerFor(child).stream()
                            .filter(line -> line.balance().signum() > 0)
                            .count();

                    return new PortalSummaryResponse.ChildSummary(
                            child.getId(),
                            child.getFullName(),
                            child.getAdmissionNumber(),
                            child.getGrade() != null ? child.getGrade().getName() : null,
                            child.getClassroom() != null ? child.getClassroom().getName() : null,
                            attendanceRate(recorded, present),
                            outstanding);
                })
                .toList();

        return new PortalSummaryResponse.ParentSummary(children.size(), children);
    }

    /** Whole percent, or null when no register has been taken — not a misleading zero. */
    private Integer attendanceRate(long recorded, long present) {
        return recorded == 0 ? null : (int) Math.round((present * 100.0) / recorded);
    }

    // ---------------------------------------------------------------- teacher

    @Override
    @Transactional(readOnly = true)
    public List<PortalClassResponse> getMyClasses() {
        List<Long> classroomIds = portalAccess.visibleClassroomIds();
        if (classroomIds.isEmpty()) {
            return List.of();
        }

        // The caller's own subjects per class, so two teachers sharing a class each see
        // only what they themselves teach.
        Map<Long, Set<Subject>> subjectsByClassroom = new LinkedHashMap<>();
        for (TeachingAssignment assignment : teachingAssignmentRepository
                .findByTeacherUserAccountId(portalAccess.currentUser().getId())) {
            if (assignment.getSubject() == null) {
                continue;
            }
            subjectsByClassroom
                    .computeIfAbsent(assignment.getClassroom().getId(), key -> new LinkedHashSet<>())
                    .add(assignment.getSubject());
        }

        Set<Long> homeroomIds = Set.copyOf(
                classroomRepository.findIdsByClassTeacherId(portalAccess.currentUser().getId()));

        return classroomRepository.findByIdInOrderByNameAsc(classroomIds).stream()
                .map(classroom -> portalMapper.toClassResponse(
                        classroom,
                        subjectsByClassroom.getOrDefault(classroom.getId(), Set.of()),
                        homeroomIds.contains(classroom.getId()),
                        studentRepository.countByClassroomId(classroom.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> getClassRoster(Long classroomId) {
        portalAccess.requireClassroomVisible(classroomId);
        return studentRepository.findByClassroomIdOrderByLastNameAscFirstNameAsc(classroomId)
                .stream().map(studentMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExamResponse> getMyExams() {
        List<Long> classroomIds = portalAccess.visibleClassroomIds();
        if (classroomIds.isEmpty()) {
            return List.of();
        }

        List<Classroom> classrooms = classroomRepository.findByIdInOrderByNameAsc(classroomIds);
        Set<Long> gradeIds = classrooms.stream()
                .map(Classroom::getGrade)
                .filter(grade -> grade != null)
                .map(grade -> grade.getId())
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
        Set<Long> visibleClassroomIds = Set.copyOf(classroomIds);

        Long schoolId = classrooms.isEmpty() ? null : classrooms.get(0).getSchool().getId();
        if (schoolId == null) {
            return List.of();
        }

        // A paper is markable when it names one of the caller's classes, or when it is
        // grade-wide over a grade they teach — the same rule the write path applies.
        return examRepository.findBySchoolIdOrderByExamDateDescTitleAsc(schoolId).stream()
                .filter(exam -> exam.getClassroom() != null
                        ? visibleClassroomIds.contains(exam.getClassroom().getId())
                        : exam.getGrade() != null && gradeIds.contains(exam.getGrade().getId()))
                .map(exam -> examMapper.toResponse(exam, examResultRepository.countByExamId(exam.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AbsenceNoteResponse> getAbsenceNotesForReview() {
        List<Long> classroomIds = portalAccess.visibleClassroomIds();
        if (classroomIds.isEmpty()) {
            return List.of();
        }
        return absenceNoteRepository.findByStudentClassroomIdInOrderByAbsenceDateDesc(classroomIds)
                .stream().map(portalMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public AbsenceNoteResponse reviewAbsenceNote(Long id, AbsenceNoteReviewRequest request) {
        AbsenceNote note = absenceNoteRepository.findWithRelationsById(id)
                .orElseThrow(() -> NotFoundException.of("Absence note", id));

        if (request.status() == AbsenceNoteStatus.SUBMITTED) {
            throw new BadRequestException("Choose whether to acknowledge or reject the note");
        }

        // A teacher may action a note for one of their own students; an admin any note in
        // their school. The classroom is the student's, since the note has no class of its own.
        User reviewer = portalAccess.currentUser();
        Student student = note.getStudent();
        portalAccess.requireClassroomWriteAccess(
                student.getClassroom() != null ? student.getClassroom().getId() : null,
                note.getSchool().getId());

        note.setStatus(request.status());
        note.setReviewedBy(reviewer);
        note.setReviewedAt(LocalDateTime.now());
        note.setReviewNote(trimToNull(request.reviewNote()));

        return portalMapper.toResponse(absenceNoteRepository.save(note));
    }

    // ------------------------------------------------------- student / parent

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> getMyChildren() {
        Parent parent = portalAccess.currentParent();
        return studentGuardianRepository
                .findByParentIdOrderByStudentLastNameAscStudentFirstNameAsc(parent.getId())
                .stream()
                .map(StudentGuardian::getStudent)
                .map(studentMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAttendance(Long studentId) {
        Student student = resolveReadableStudent(studentId);
        return attendanceRepository.findByStudentIdOrderByAttendanceDateDesc(student.getId())
                .stream().map(enrollmentMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExamResultResponse> getResults(Long studentId) {
        Student student = resolveReadableStudent(studentId);
        return examResultRepository.findByStudentIdOrderByExamExamDateDesc(student.getId())
                .stream().map(examMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentFeeLedgerResponse> getFees(Long studentId) {
        return ledgerFor(resolveReadableStudent(studentId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AbsenceNoteResponse> getAbsenceNotes(Long studentId) {
        Student student = resolveReadableStudent(studentId);
        return absenceNoteRepository.findByStudentIdOrderByAbsenceDateDesc(student.getId())
                .stream().map(portalMapper::toResponse).toList();
    }

    @Override
    @Transactional
    public AbsenceNoteResponse submitAbsenceNote(AbsenceNoteRequest request) {
        Student student = resolveReadableStudent(request.studentId());
        User submitter = portalAccess.currentUser();

        if (absenceNoteRepository.existsByStudentIdAndAbsenceDate(student.getId(), request.absenceDate())) {
            throw new DuplicateResourceException(
                    "A note has already been submitted for " + request.absenceDate()
                            + ". Ask the school to update it instead.");
        }

        AbsenceNote note = AbsenceNote.builder()
                .absenceDate(request.absenceDate())
                .reason(request.reason().trim())
                .status(AbsenceNoteStatus.SUBMITTED)
                .submittedBy(submitter)
                .student(student)
                .school(student.getSchool())
                .build();

        return portalMapper.toResponse(absenceNoteRepository.save(note));
    }

    @Override
    @Transactional
    public void acknowledgeFee(Long feeStructureId, Long studentId) {
        Student student = resolveReadableStudent(studentId);
        User user = portalAccess.currentUser();

        FeeStructure fee = feeStructureRepository.findWithRelationsById(feeStructureId)
                .orElseThrow(() -> NotFoundException.of("Fee item", feeStructureId));

        if (!fee.getSchool().getId().equals(student.getSchool().getId())
                || student.getGrade() == null
                || !fee.getGrade().getId().equals(student.getGrade().getId())) {
            throw new BadRequestException("That fee item does not apply to this student");
        }

        // Idempotent: clicking twice is a no-op rather than a duplicate-key error.
        if (feeAcknowledgementRepository.existsByStudentIdAndFeeStructureIdAndAcknowledgedById(
                student.getId(), fee.getId(), user.getId())) {
            return;
        }

        feeAcknowledgementRepository.save(FeeAcknowledgement.builder()
                .student(student)
                .feeStructure(fee)
                .acknowledgedBy(user)
                .acknowledgedAt(LocalDateTime.now())
                .school(student.getSchool())
                .build());
    }

    // ------------------------------------------------------------------ helpers

    /**
     * The student a read applies to.
     *
     * <p>A student may only ever read their own record, so {@code studentId} is ignored
     * for them rather than checked — there is nothing else it could legitimately name. A
     * parent must say which child, and the id is verified against their own links.
     */
    private Student resolveReadableStudent(Long studentId) {
        User user = portalAccess.currentUser();

        if (user.getRole() == UserRole.STUDENT) {
            return portalAccess.currentStudent();
        }
        if (user.getRole() == UserRole.PARENT) {
            if (studentId == null) {
                throw new BadRequestException("Choose which child you want to view");
            }
            portalAccess.requireStudentVisible(studentId);
            return studentRepository.findWithRelationsById(studentId)
                    .orElseThrow(() -> NotFoundException.of("Student", studentId));
        }
        if (user.getRole() == UserRole.TEACHER) {
            if (studentId == null) {
                throw new BadRequestException("Choose a student");
            }
            portalAccess.requireStudentVisible(studentId);
            return studentRepository.findWithRelationsById(studentId)
                    .orElseThrow(() -> NotFoundException.of("Student", studentId));
        }
        throw new AccessDeniedAppException("This view is only available to teachers, students and parents");
    }

    /**
     * One student's fee ledger. Mirrors {@code FeeServiceImpl.getLedgerForStudent} but
     * without its admin gate; the ledger arithmetic itself lives in {@link FeeMapper}, so
     * the two views cannot disagree about what is owed.
     */
    private List<StudentFeeLedgerResponse> ledgerFor(Student student) {
        if (student.getGrade() == null) {
            return List.of();
        }
        Long schoolId = student.getSchool().getId();

        List<StudentFeeLedgerResponse> ledger = new ArrayList<>();
        for (FeeStructure fee : feeStructureRepository.findByGradeIdOrderByNameAsc(student.getGrade().getId())) {
            if (!fee.getSchool().getId().equals(schoolId)) {
                continue;
            }
            BigDecimal paid = feePaymentRepository
                    .findByStudentIdAndFeeStructureIdAndStatusOrderByPaymentDateAsc(
                            student.getId(), fee.getId(), PaymentStatus.COMPLETED)
                    .stream()
                    .map(payment -> payment.getAmountPaid() == null ? BigDecimal.ZERO : payment.getAmountPaid())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            ledger.add(feeMapper.toLedger(student, fee, paid));
        }
        return ledger;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
