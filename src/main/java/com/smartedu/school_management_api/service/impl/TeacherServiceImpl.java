package com.smartedu.school_management_api.service.impl;

import com.smartedu.school_management_api.dto.teacher.TeacherRequest;
import com.smartedu.school_management_api.dto.teacher.TeacherResponse;
import com.smartedu.school_management_api.entity.School;
import com.smartedu.school_management_api.entity.Subject;
import com.smartedu.school_management_api.entity.Teacher;
import com.smartedu.school_management_api.entity.TeacherStatus;
import com.smartedu.school_management_api.entity.User;
import com.smartedu.school_management_api.entity.UserRole;
import com.smartedu.school_management_api.exception.BadRequestException;
import com.smartedu.school_management_api.exception.DuplicateResourceException;
import com.smartedu.school_management_api.exception.NotFoundException;
import com.smartedu.school_management_api.mapper.StaffMapper;
import com.smartedu.school_management_api.repository.AttendanceRepository;
import com.smartedu.school_management_api.repository.SectionRepository;
import com.smartedu.school_management_api.repository.SubjectRepository;
import com.smartedu.school_management_api.repository.TeacherRepository;
import com.smartedu.school_management_api.repository.TeachingAssignmentRepository;
import com.smartedu.school_management_api.repository.UserRepository;
import com.smartedu.school_management_api.service.SchoolAccessService;
import com.smartedu.school_management_api.service.TeacherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeacherServiceImpl implements TeacherService {

    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final SectionRepository sectionRepository;
    private final AttendanceRepository attendanceRepository;
    private final TeachingAssignmentRepository teachingAssignmentRepository;
    private final SchoolAccessService access;
    private final StaffMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<TeacherResponse> getAllTeachers() {
        Long schoolId = access.schoolScopeForCurrentUser();
        List<Teacher> teachers = schoolId == null
                ? teacherRepository.findAllByOrderByLastNameAscFirstNameAsc()
                : teacherRepository.findBySchoolIdOrderByLastNameAscFirstNameAsc(schoolId);
        return teachers.stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TeacherResponse getTeacherById(Long id) {
        return mapper.toResponse(loadAccessible(id));
    }

    @Override
    @Transactional
    public TeacherResponse createTeacher(TeacherRequest request) {
        // Unlike the grade-derived entities, a teacher has no parent record to take a
        // school from, so the tenant is resolved from the caller directly.
        School school = access.resolveWritableSchool(null);
        String employeeNumber = request.employeeNumber().trim();

        if (teacherRepository.existsBySchoolIdAndEmployeeNumberIgnoreCase(school.getId(), employeeNumber)) {
            throw new DuplicateResourceException(
                    "A teacher with employee number '" + employeeNumber + "' already exists in this school");
        }

        validateHireDate(request);

        Teacher teacher = Teacher.builder()
                .employeeNumber(employeeNumber)
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .gender(request.gender())
                .dateOfBirth(request.dateOfBirth())
                .email(normalizeEmail(request.email()))
                .phoneNumber(trimToNull(request.phoneNumber()))
                .address(trimToNull(request.address()))
                .qualification(trimToNull(request.qualification()))
                .specialization(trimToNull(request.specialization()))
                .hireDate(request.hireDate())
                .status(request.status() == null ? TeacherStatus.ACTIVE : request.status())
                .subjects(resolveSubjects(request.subjectIds(), school.getId()))
                .userAccount(resolveUserAccount(request.userAccountId(), school.getId(), null))
                .school(school)
                .build();

        return mapper.toResponse(teacherRepository.save(teacher));
    }

    @Override
    @Transactional
    public TeacherResponse updateTeacher(Long id, TeacherRequest request) {
        Teacher teacher = loadAccessible(id);
        Long schoolId = teacher.getSchool().getId();
        String employeeNumber = request.employeeNumber().trim();

        if (teacherRepository.existsBySchoolIdAndEmployeeNumberIgnoreCaseAndIdNot(schoolId, employeeNumber, id)) {
            throw new DuplicateResourceException(
                    "A teacher with employee number '" + employeeNumber + "' already exists in this school");
        }

        validateHireDate(request);

        teacher.setEmployeeNumber(employeeNumber);
        teacher.setFirstName(request.firstName().trim());
        teacher.setLastName(request.lastName().trim());
        teacher.setGender(request.gender());
        teacher.setDateOfBirth(request.dateOfBirth());
        teacher.setEmail(normalizeEmail(request.email()));
        teacher.setPhoneNumber(trimToNull(request.phoneNumber()));
        teacher.setAddress(trimToNull(request.address()));
        teacher.setQualification(trimToNull(request.qualification()));
        teacher.setSpecialization(trimToNull(request.specialization()));
        teacher.setHireDate(request.hireDate());
        if (request.status() != null) {
            teacher.setStatus(request.status());
        }
        // Replace in place: the collection is the owning side of teacher_subjects.
        teacher.getSubjects().clear();
        teacher.getSubjects().addAll(resolveSubjects(request.subjectIds(), schoolId));
        teacher.setUserAccount(resolveUserAccount(request.userAccountId(), schoolId, id));

        return mapper.toResponse(teacherRepository.save(teacher));
    }

    @Override
    @Transactional
    public void deleteTeacher(Long id) {
        Teacher teacher = loadAccessible(id);

        // Sections point at a teacher with a nullable FK, but silently unheading a
        // section on delete would hide the change from whoever set it.
        long headedSections = sectionRepository.countBySectionHeadId(id);
        if (headedSections > 0) {
            throw new BadRequestException("This teacher heads " + headedSections
                    + " section(s). Reassign them before deleting the teacher.");
        }

        long registers = attendanceRepository.countByRecordedById(id);
        if (registers > 0) {
            throw new BadRequestException("This teacher has recorded " + registers
                    + " attendance entr(ies) and cannot be deleted. Mark them as Resigned instead.");
        }

        // Assignments are what grant portal access, so removing them silently would
        // change who can see a class without saying so.
        long assignments = teachingAssignmentRepository.countByTeacherId(id);
        if (assignments > 0) {
            throw new BadRequestException("This teacher has " + assignments
                    + " teaching assignment(s). Remove them before deleting the teacher.");
        }

        teacherRepository.delete(teacher);
    }

    // ------------------------------------------------------------------ helpers

    private Teacher loadAccessible(Long id) {
        Teacher teacher = teacherRepository.findWithRelationsById(id)
                .orElseThrow(() -> NotFoundException.of("Teacher", id));
        access.requireSchoolAccess(teacher.getSchool().getId());
        return teacher;
    }

    /** Every subject must belong to the teacher's school, or a super admin could mix tenants. */
    private Set<Subject> resolveSubjects(Set<Long> subjectIds, Long schoolId) {
        if (subjectIds == null || subjectIds.isEmpty()) {
            return new LinkedHashSet<>();
        }
        Set<Subject> subjects = new LinkedHashSet<>();
        for (Long subjectId : subjectIds) {
            Subject subject = subjectRepository.findWithRelationsById(subjectId)
                    .orElseThrow(() -> NotFoundException.of("Subject", subjectId));
            if (!subject.getSchool().getId().equals(schoolId)) {
                throw new BadRequestException(
                        "Subject '" + subject.getName() + "' belongs to a different school");
            }
            subjects.add(subject);
        }
        return subjects;
    }

    /**
     * Links an optional login. The account must be a {@code TEACHER} in the same
     * school and not already claimed by another teacher record.
     */
    private User resolveUserAccount(UUID userAccountId, Long schoolId, Long currentTeacherId) {
        if (userAccountId == null) {
            return null;
        }
        User user = userRepository.findWithSchoolById(userAccountId)
                .orElseThrow(() -> NotFoundException.of("User", userAccountId));

        if (user.getRole() != UserRole.TEACHER) {
            throw new BadRequestException("The selected user account is not a teacher");
        }
        if (user.schoolIdOrNull() == null || !user.schoolIdOrNull().equals(schoolId)) {
            throw new BadRequestException("The selected user account belongs to a different school");
        }

        boolean claimedByAnother = teacherRepository.findByUserAccountId(userAccountId)
                .filter(existing -> currentTeacherId == null || !existing.getId().equals(currentTeacherId))
                .isPresent();
        if (claimedByAnother) {
            throw new DuplicateResourceException("That user account is already linked to another teacher");
        }
        return user;
    }

    private void validateHireDate(TeacherRequest request) {
        if (request.dateOfBirth() != null && request.hireDate().isBefore(request.dateOfBirth())) {
            throw new BadRequestException("The hire date cannot be before the date of birth");
        }
        if (request.hireDate().isAfter(LocalDate.now().plusYears(1))) {
            throw new BadRequestException("The hire date is too far in the future");
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
