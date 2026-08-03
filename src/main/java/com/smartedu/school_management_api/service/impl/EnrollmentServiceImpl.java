package com.smartedu.school_management_api.service.impl;

import com.smartedu.school_management_api.dto.enrollment.EnrollmentRequest;
import com.smartedu.school_management_api.dto.enrollment.EnrollmentResponse;
import com.smartedu.school_management_api.entity.AcademicYear;
import com.smartedu.school_management_api.entity.Classroom;
import com.smartedu.school_management_api.entity.Enrollment;
import com.smartedu.school_management_api.entity.EnrollmentStatus;
import com.smartedu.school_management_api.entity.Grade;
import com.smartedu.school_management_api.entity.Student;
import com.smartedu.school_management_api.exception.BadRequestException;
import com.smartedu.school_management_api.exception.DuplicateResourceException;
import com.smartedu.school_management_api.exception.NotFoundException;
import com.smartedu.school_management_api.mapper.EnrollmentMapper;
import com.smartedu.school_management_api.repository.AcademicYearRepository;
import com.smartedu.school_management_api.repository.ClassroomRepository;
import com.smartedu.school_management_api.repository.EnrollmentRepository;
import com.smartedu.school_management_api.repository.GradeRepository;
import com.smartedu.school_management_api.repository.StudentRepository;
import com.smartedu.school_management_api.service.EnrollmentService;
import com.smartedu.school_management_api.service.SchoolAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    /** Statuses that still occupy a place in a classroom. */
    private static final List<EnrollmentStatus> OPEN_STATUSES =
            List.of(EnrollmentStatus.ENROLLED, EnrollmentStatus.REPEATING);

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final AcademicYearRepository academicYearRepository;
    private final GradeRepository gradeRepository;
    private final ClassroomRepository classroomRepository;
    private final SchoolAccessService access;
    private final EnrollmentMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getAllEnrollments() {
        Long schoolId = access.schoolScopeForCurrentUser();
        List<Enrollment> enrollments = schoolId == null
                ? enrollmentRepository.findAllByOrderByAcademicYearStartDateDescStudentLastNameAsc()
                : enrollmentRepository.findBySchoolIdOrderByAcademicYearStartDateDescStudentLastNameAsc(schoolId);
        return enrollments.stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EnrollmentResponse getEnrollmentById(Long id) {
        return mapper.toResponse(loadAccessible(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrollmentsForStudent(Long studentId) {
        Student student = loadAccessibleStudent(studentId);
        return enrollmentRepository.findByStudentIdOrderByAcademicYearStartDateDesc(student.getId())
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional
    public EnrollmentResponse createEnrollment(EnrollmentRequest request) {
        // The student fixes the school, so a caller cannot enrol into a tenant they
        // cannot reach even by naming a foreign year or classroom.
        Student student = loadAccessibleStudent(request.studentId());
        Long schoolId = student.getSchool().getId();

        AcademicYear year = loadAccessibleYear(request.academicYearId(), schoolId);
        Grade grade = loadAccessibleGrade(request.gradeId(), schoolId);

        if (enrollmentRepository.existsByStudentIdAndAcademicYearId(student.getId(), year.getId())) {
            throw new DuplicateResourceException(
                    student.getFullName() + " is already enrolled for " + year.getName());
        }

        Classroom classroom = resolveClassroom(request.classroomId(), grade, year, schoolId, null);
        validateDates(request);

        Enrollment enrollment = Enrollment.builder()
                .rollNumber(trimToNull(request.rollNumber()))
                .enrollmentDate(request.enrollmentDate())
                .completionDate(request.completionDate())
                .status(request.status() == null ? EnrollmentStatus.ENROLLED : request.status())
                .remarks(trimToNull(request.remarks()))
                .student(student)
                .academicYear(year)
                .grade(grade)
                .classroom(classroom)
                .school(student.getSchool())
                .build();

        Enrollment saved = enrollmentRepository.save(enrollment);
        syncStudentPlacement(saved);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public EnrollmentResponse updateEnrollment(Long id, EnrollmentRequest request) {
        Enrollment enrollment = loadAccessible(id);
        Student student = loadAccessibleStudent(request.studentId());
        Long schoolId = student.getSchool().getId();

        AcademicYear year = loadAccessibleYear(request.academicYearId(), schoolId);
        Grade grade = loadAccessibleGrade(request.gradeId(), schoolId);

        if (enrollmentRepository.existsByStudentIdAndAcademicYearIdAndIdNot(student.getId(), year.getId(), id)) {
            throw new DuplicateResourceException(
                    student.getFullName() + " is already enrolled for " + year.getName());
        }

        Classroom classroom = resolveClassroom(request.classroomId(), grade, year, schoolId, id);
        validateDates(request);

        enrollment.setRollNumber(trimToNull(request.rollNumber()));
        enrollment.setEnrollmentDate(request.enrollmentDate());
        enrollment.setCompletionDate(request.completionDate());
        if (request.status() != null) {
            enrollment.setStatus(request.status());
        }
        enrollment.setRemarks(trimToNull(request.remarks()));
        enrollment.setStudent(student);
        enrollment.setAcademicYear(year);
        enrollment.setGrade(grade);
        enrollment.setClassroom(classroom);
        enrollment.setSchool(student.getSchool());

        Enrollment saved = enrollmentRepository.save(enrollment);
        syncStudentPlacement(saved);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteEnrollment(Long id) {
        enrollmentRepository.delete(loadAccessible(id));
    }

    // ------------------------------------------------------------------ helpers

    private Enrollment loadAccessible(Long id) {
        Enrollment enrollment = enrollmentRepository.findWithRelationsById(id)
                .orElseThrow(() -> NotFoundException.of("Enrollment", id));
        access.requireSchoolAccess(enrollment.getSchool().getId());
        return enrollment;
    }

    private Student loadAccessibleStudent(Long studentId) {
        Student student = studentRepository.findWithRelationsById(studentId)
                .orElseThrow(() -> NotFoundException.of("Student", studentId));
        access.requireSchoolAccess(student.getSchool().getId());
        return student;
    }

    private AcademicYear loadAccessibleYear(Long yearId, Long schoolId) {
        AcademicYear year = academicYearRepository.findWithSchoolById(yearId)
                .orElseThrow(() -> NotFoundException.of("Academic year", yearId));
        if (!year.getSchool().getId().equals(schoolId)) {
            throw new BadRequestException("The academic year belongs to a different school");
        }
        return year;
    }

    private Grade loadAccessibleGrade(Long gradeId, Long schoolId) {
        Grade grade = gradeRepository.findWithSchoolById(gradeId)
                .orElseThrow(() -> NotFoundException.of("Grade", gradeId));
        if (!grade.getSchool().getId().equals(schoolId)) {
            throw new BadRequestException("The grade belongs to a different school");
        }
        return grade;
    }

    /**
     * Validates the optional class placement and checks it is not already full.
     *
     * @param currentEnrollmentId excluded from the occupancy count on update, so
     *                            re-saving an enrolment already in the class cannot
     *                            trip the capacity check
     */
    private Classroom resolveClassroom(Long classroomId, Grade grade, AcademicYear year,
                                       Long schoolId, Long currentEnrollmentId) {
        if (classroomId == null) {
            return null;
        }
        Classroom classroom = classroomRepository.findWithRelationsById(classroomId)
                .orElseThrow(() -> NotFoundException.of("Classroom", classroomId));

        if (!classroom.getSchool().getId().equals(schoolId)) {
            throw new BadRequestException("The classroom belongs to a different school");
        }
        if (!classroom.getGrade().getId().equals(grade.getId())) {
            throw new BadRequestException("The selected classroom belongs to a different grade");
        }
        if (!classroom.getAcademicYear().getId().equals(year.getId())) {
            throw new BadRequestException("The selected classroom belongs to a different academic year");
        }

        if (classroom.getCapacity() != null) {
            long occupancy = enrollmentRepository
                    .findByClassroomIdAndStatusIn(classroomId, OPEN_STATUSES).stream()
                    .filter(e -> currentEnrollmentId == null || !e.getId().equals(currentEnrollmentId))
                    .count();
            if (occupancy >= classroom.getCapacity()) {
                throw new BadRequestException("Classroom '" + classroom.getName()
                        + "' is full (" + classroom.getCapacity() + " places)");
            }
        }
        return classroom;
    }

    /**
     * Mirrors an open enrolment onto the student's current placement.
     *
     * <p>Only the current academic year writes back: the student record holds where
     * they are <em>now</em>, so correcting an old year's history must not move them.
     */
    private void syncStudentPlacement(Enrollment enrollment) {
        if (!Boolean.TRUE.equals(enrollment.getAcademicYear().getCurrent())) {
            return;
        }
        if (enrollment.getStatus() == null || !enrollment.getStatus().isOpen()) {
            return;
        }
        Student student = enrollment.getStudent();
        student.setGrade(enrollment.getGrade());
        student.setClassroom(enrollment.getClassroom());
        studentRepository.save(student);
    }

    private void validateDates(EnrollmentRequest request) {
        if (request.completionDate() != null
                && request.completionDate().isBefore(request.enrollmentDate())) {
            throw new BadRequestException("The completion date cannot be before the enrollment date");
        }
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
