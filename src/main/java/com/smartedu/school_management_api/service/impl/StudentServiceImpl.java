package com.smartedu.school_management_api.service.impl;

import com.smartedu.school_management_api.dto.common.PageResponse;
import com.smartedu.school_management_api.dto.student.StudentRequest;
import com.smartedu.school_management_api.dto.student.StudentResponse;
import com.smartedu.school_management_api.entity.Classroom;
import com.smartedu.school_management_api.entity.Grade;
import com.smartedu.school_management_api.entity.Student;
import com.smartedu.school_management_api.entity.StudentStatus;
import com.smartedu.school_management_api.exception.BadRequestException;
import com.smartedu.school_management_api.exception.DuplicateResourceException;
import com.smartedu.school_management_api.exception.NotFoundException;
import com.smartedu.school_management_api.mapper.StudentMapper;
import com.smartedu.school_management_api.repository.ClassroomRepository;
import com.smartedu.school_management_api.repository.GradeRepository;
import com.smartedu.school_management_api.repository.StudentRepository;
import com.smartedu.school_management_api.service.SchoolAccessService;
import com.smartedu.school_management_api.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final GradeRepository gradeRepository;
    private final ClassroomRepository classroomRepository;
    private final SchoolAccessService access;
    private final StudentMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> getAllStudents() {
        Long schoolId = access.schoolScopeForCurrentUser();
        return studentRepository.findBySchoolIdOrderByLastNameAscFirstNameAsc(schoolId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StudentResponse> searchStudents(String search, StudentStatus status,
                                                       Long gradeId, Long classroomId, Pageable pageable) {
        // Always a concrete school: the caller is a school admin by the time this returns.
        Long schoolId = access.schoolScopeForCurrentUser();
        String term = (search == null || search.isBlank()) ? null : search.trim();

        Page<Student> page = studentRepository.search(schoolId, status, gradeId, classroomId, term, pageable);
        return PageResponse.from(page, mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse getStudentById(Long id) {
        return mapper.toResponse(loadAccessible(id));
    }

    @Override
    @Transactional
    public StudentResponse createStudent(StudentRequest request) {
        Grade grade = loadAccessibleGrade(request.gradeId());
        Long schoolId = grade.getSchool().getId();

        String admissionNumber = request.admissionNumber().trim();
        if (studentRepository.existsBySchoolIdAndAdmissionNumberIgnoreCase(schoolId, admissionNumber)) {
            throw new DuplicateResourceException(
                    "A student with admission number '" + admissionNumber + "' already exists in this school");
        }

        Classroom classroom = resolveClassroom(request.classroomId(), grade, null);
        validateEnrollmentDate(request);

        Student student = Student.builder()
                .admissionNumber(admissionNumber)
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .gender(request.gender())
                .dateOfBirth(request.dateOfBirth())
                .email(normalizeEmail(request.email()))
                .phoneNumber(trimToNull(request.phoneNumber()))
                .address(trimToNull(request.address()))
                .photoUrl(trimToNull(request.photoUrl()))
                .guardianName(trimToNull(request.guardianName()))
                .guardianPhone(trimToNull(request.guardianPhone()))
                .guardianEmail(normalizeEmail(request.guardianEmail()))
                .enrollmentDate(request.enrollmentDate())
                .status(request.status() == null ? StudentStatus.ACTIVE : request.status())
                .grade(grade)
                .classroom(classroom)
                .school(grade.getSchool())
                .build();

        return mapper.toResponse(studentRepository.save(student));
    }

    @Override
    @Transactional
    public StudentResponse updateStudent(Long id, StudentRequest request) {
        Student student = loadAccessible(id);
        Grade grade = loadAccessibleGrade(request.gradeId());
        Long schoolId = grade.getSchool().getId();

        String admissionNumber = request.admissionNumber().trim();
        if (studentRepository.existsBySchoolIdAndAdmissionNumberIgnoreCaseAndIdNot(schoolId, admissionNumber, id)) {
            throw new DuplicateResourceException(
                    "A student with admission number '" + admissionNumber + "' already exists in this school");
        }

        Classroom classroom = resolveClassroom(request.classroomId(), grade, student.getId());
        validateEnrollmentDate(request);

        student.setAdmissionNumber(admissionNumber);
        student.setFirstName(request.firstName().trim());
        student.setLastName(request.lastName().trim());
        student.setGender(request.gender());
        student.setDateOfBirth(request.dateOfBirth());
        student.setEmail(normalizeEmail(request.email()));
        student.setPhoneNumber(trimToNull(request.phoneNumber()));
        student.setAddress(trimToNull(request.address()));
        student.setPhotoUrl(trimToNull(request.photoUrl()));
        student.setGuardianName(trimToNull(request.guardianName()));
        student.setGuardianPhone(trimToNull(request.guardianPhone()));
        student.setGuardianEmail(normalizeEmail(request.guardianEmail()));
        student.setEnrollmentDate(request.enrollmentDate());
        if (request.status() != null) {
            student.setStatus(request.status());
        }
        student.setGrade(grade);
        student.setClassroom(classroom);
        student.setSchool(grade.getSchool());

        return mapper.toResponse(studentRepository.save(student));
    }

    @Override
    @Transactional
    public void deleteStudent(Long id) {
        studentRepository.delete(loadAccessible(id));
    }

    // ------------------------------------------------------------------ helpers

    private Student loadAccessible(Long id) {
        Student student = studentRepository.findWithRelationsById(id)
                .orElseThrow(() -> NotFoundException.of("Student", id));
        access.requireSchoolAccess(student.getSchool().getId());
        return student;
    }

    private Grade loadAccessibleGrade(Long gradeId) {
        Grade grade = gradeRepository.findWithSchoolById(gradeId)
                .orElseThrow(() -> NotFoundException.of("Grade", gradeId));
        access.requireSchoolAccess(grade.getSchool().getId());
        return grade;
    }

    /**
     * Validates the optional class placement: same grade, and not already full.
     *
     * @param currentStudentId excluded from the occupancy count on update, so re-saving
     *                         a student already in the class cannot trip the capacity check
     */
    private Classroom resolveClassroom(Long classroomId, Grade grade, Long currentStudentId) {
        if (classroomId == null) {
            return null;
        }
        Classroom classroom = classroomRepository.findWithRelationsById(classroomId)
                .orElseThrow(() -> NotFoundException.of("Classroom", classroomId));
        access.requireSchoolAccess(classroom.getSchool().getId());

        if (!classroom.getGrade().getId().equals(grade.getId())) {
            throw new BadRequestException("The selected classroom belongs to a different grade");
        }

        if (classroom.getCapacity() != null) {
            long occupancy = studentRepository.countByClassroomId(classroomId);
            boolean alreadyInClass = currentStudentId != null && studentRepository
                    .findByClassroomIdOrderByLastNameAscFirstNameAsc(classroomId).stream()
                    .anyMatch(s -> s.getId().equals(currentStudentId));
            if (alreadyInClass) {
                occupancy--;
            }
            if (occupancy >= classroom.getCapacity()) {
                throw new BadRequestException("Classroom '" + classroom.getName()
                        + "' is full (" + classroom.getCapacity() + " places)");
            }
        }
        return classroom;
    }

    private void validateEnrollmentDate(StudentRequest request) {
        if (request.enrollmentDate().isBefore(request.dateOfBirth())) {
            throw new BadRequestException("The enrollment date cannot be before the date of birth");
        }
        if (request.enrollmentDate().isAfter(LocalDate.now().plusYears(1))) {
            throw new BadRequestException("The enrollment date is too far in the future");
        }
    }

    private static String normalizeEmail(String email) {
        String trimmed = trimToNull(email);
        return trimmed == null ? null : trimmed.toLowerCase();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
