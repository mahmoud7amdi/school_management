package com.smartedu.school_management_api.service.impl;

import com.smartedu.school_management_api.dto.classroom.ClassroomRequest;
import com.smartedu.school_management_api.dto.classroom.ClassroomResponse;
import com.smartedu.school_management_api.entity.AcademicYear;
import com.smartedu.school_management_api.entity.Classroom;
import com.smartedu.school_management_api.entity.Grade;
import com.smartedu.school_management_api.entity.Section;
import com.smartedu.school_management_api.entity.User;
import com.smartedu.school_management_api.entity.UserRole;
import com.smartedu.school_management_api.exception.BadRequestException;
import com.smartedu.school_management_api.exception.DuplicateResourceException;
import com.smartedu.school_management_api.exception.NotFoundException;
import com.smartedu.school_management_api.mapper.ClassroomMapper;
import com.smartedu.school_management_api.repository.AcademicYearRepository;
import com.smartedu.school_management_api.repository.ClassroomRepository;
import com.smartedu.school_management_api.repository.GradeRepository;
import com.smartedu.school_management_api.repository.SectionRepository;
import com.smartedu.school_management_api.repository.StudentRepository;
import com.smartedu.school_management_api.repository.UserRepository;
import com.smartedu.school_management_api.service.ClassroomService;
import com.smartedu.school_management_api.service.SchoolAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClassroomServiceImpl implements ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final GradeRepository gradeRepository;
    private final AcademicYearRepository academicYearRepository;
    private final SectionRepository sectionRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final SchoolAccessService access;
    private final ClassroomMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<ClassroomResponse> getAllClassrooms() {
        Long schoolId = access.schoolScopeForCurrentUser();
        List<Classroom> classrooms = schoolId == null
                ? classroomRepository.findAllByOrderByNameAsc()
                : classroomRepository.findBySchoolIdOrderByNameAsc(schoolId);

        return classrooms.stream()
                .map(c -> mapper.toResponse(c, studentRepository.countByClassroomId(c.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClassroomResponse getClassroomById(Long id) {
        Classroom classroom = loadAccessible(id);
        return mapper.toResponse(classroom, studentRepository.countByClassroomId(id));
    }

    @Override
    @Transactional
    public ClassroomResponse createClassroom(ClassroomRequest request) {
        Grade grade = loadAccessibleGrade(request.gradeId());
        AcademicYear year = loadAccessibleYear(request.academicYearId());
        assertSameSchool(grade, year);

        String name = request.name().trim();
        if (classroomRepository.existsByGradeIdAndAcademicYearIdAndNameIgnoreCase(
                grade.getId(), year.getId(), name)) {
            throw new DuplicateResourceException(
                    "A classroom named '" + name + "' already exists for this grade and academic year");
        }

        Classroom classroom = Classroom.builder()
                .name(name)
                .capacity(request.capacity())
                .roomNumber(trimToNull(request.roomNumber()))
                .grade(grade)
                .academicYear(year)
                .section(resolveSection(request.sectionId(), grade))
                .classTeacher(resolveTeacher(request.classTeacherId(), grade.getSchool().getId()))
                .school(grade.getSchool())
                .build();

        return mapper.toResponse(classroomRepository.save(classroom), 0L);
    }

    @Override
    @Transactional
    public ClassroomResponse updateClassroom(Long id, ClassroomRequest request) {
        Classroom classroom = loadAccessible(id);
        Grade grade = loadAccessibleGrade(request.gradeId());
        AcademicYear year = loadAccessibleYear(request.academicYearId());
        assertSameSchool(grade, year);

        String name = request.name().trim();
        if (classroomRepository.existsByGradeIdAndAcademicYearIdAndNameIgnoreCaseAndIdNot(
                grade.getId(), year.getId(), name, id)) {
            throw new DuplicateResourceException(
                    "A classroom named '" + name + "' already exists for this grade and academic year");
        }

        long occupancy = studentRepository.countByClassroomId(id);
        if (request.capacity() != null && occupancy > request.capacity()) {
            throw new BadRequestException("Capacity cannot be below the current enrolment of " + occupancy);
        }

        classroom.setName(name);
        classroom.setCapacity(request.capacity());
        classroom.setRoomNumber(trimToNull(request.roomNumber()));
        classroom.setGrade(grade);
        classroom.setAcademicYear(year);
        classroom.setSection(resolveSection(request.sectionId(), grade));
        classroom.setClassTeacher(resolveTeacher(request.classTeacherId(), grade.getSchool().getId()));
        classroom.setSchool(grade.getSchool());

        return mapper.toResponse(classroomRepository.save(classroom), occupancy);
    }

    @Override
    @Transactional
    public void deleteClassroom(Long id) {
        Classroom classroom = loadAccessible(id);

        long studentCount = studentRepository.countByClassroomId(id);
        if (studentCount > 0) {
            throw new BadRequestException("This classroom has " + studentCount
                    + " student(s). Move them to another classroom before deleting it.");
        }
        classroomRepository.delete(classroom);
    }

    // ------------------------------------------------------------------ helpers

    private Classroom loadAccessible(Long id) {
        Classroom classroom = classroomRepository.findWithRelationsById(id)
                .orElseThrow(() -> NotFoundException.of("Classroom", id));
        access.requireSchoolAccess(classroom.getSchool().getId());
        return classroom;
    }

    private Grade loadAccessibleGrade(Long gradeId) {
        Grade grade = gradeRepository.findWithSchoolById(gradeId)
                .orElseThrow(() -> NotFoundException.of("Grade", gradeId));
        access.requireSchoolAccess(grade.getSchool().getId());
        return grade;
    }

    private AcademicYear loadAccessibleYear(Long yearId) {
        AcademicYear year = academicYearRepository.findWithSchoolById(yearId)
                .orElseThrow(() -> NotFoundException.of("Academic year", yearId));
        access.requireSchoolAccess(year.getSchool().getId());
        return year;
    }

    /** A super admin could otherwise pair a grade and a year from two different schools. */
    private void assertSameSchool(Grade grade, AcademicYear year) {
        if (!grade.getSchool().getId().equals(year.getSchool().getId())) {
            throw new BadRequestException("The grade and academic year must belong to the same school");
        }
    }

    /** The section must sit under the same grade, which also pins it to the same school. */
    private Section resolveSection(Long sectionId, Grade grade) {
        if (sectionId == null) {
            return null;
        }
        Section section = sectionRepository.findWithRelationsById(sectionId)
                .orElseThrow(() -> NotFoundException.of("Section", sectionId));
        access.requireSchoolAccess(section.getSchool().getId());

        if (!section.getGrade().getId().equals(grade.getId())) {
            throw new BadRequestException("The selected section belongs to a different grade");
        }
        return section;
    }

    private User resolveTeacher(UUID teacherId, Long schoolId) {
        if (teacherId == null) {
            return null;
        }
        User teacher = userRepository.findWithSchoolById(teacherId)
                .orElseThrow(() -> NotFoundException.of("Teacher", teacherId));
        if (teacher.getRole() != UserRole.TEACHER) {
            throw new BadRequestException("The selected user is not a teacher");
        }
        if (teacher.schoolIdOrNull() == null || !teacher.schoolIdOrNull().equals(schoolId)) {
            throw new BadRequestException("The selected teacher belongs to a different school");
        }
        return teacher;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
