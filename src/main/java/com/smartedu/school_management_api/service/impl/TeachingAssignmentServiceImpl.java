package com.smartedu.school_management_api.service.impl;

import com.smartedu.school_management_api.dto.teaching.TeachingAssignmentRequest;
import com.smartedu.school_management_api.dto.teaching.TeachingAssignmentResponse;
import com.smartedu.school_management_api.entity.Classroom;
import com.smartedu.school_management_api.entity.Subject;
import com.smartedu.school_management_api.entity.Teacher;
import com.smartedu.school_management_api.entity.TeachingAssignment;
import com.smartedu.school_management_api.exception.BadRequestException;
import com.smartedu.school_management_api.exception.DuplicateResourceException;
import com.smartedu.school_management_api.exception.NotFoundException;
import com.smartedu.school_management_api.mapper.TeachingAssignmentMapper;
import com.smartedu.school_management_api.repository.ClassroomRepository;
import com.smartedu.school_management_api.repository.SubjectRepository;
import com.smartedu.school_management_api.repository.TeacherRepository;
import com.smartedu.school_management_api.repository.TeachingAssignmentRepository;
import com.smartedu.school_management_api.service.SchoolAccessService;
import com.smartedu.school_management_api.service.TeachingAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Who teaches what, where.
 *
 * <p>The classroom is the anchor: it pins the school, the academic year and the grade, so
 * none of those are accepted from the request. That is the same reasoning
 * {@link ExamServiceImpl} applies to a subject.
 */
@Service
@RequiredArgsConstructor
public class TeachingAssignmentServiceImpl implements TeachingAssignmentService {

    private final TeachingAssignmentRepository assignmentRepository;
    private final TeacherRepository teacherRepository;
    private final ClassroomRepository classroomRepository;
    private final SubjectRepository subjectRepository;
    private final SchoolAccessService access;
    private final TeachingAssignmentMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<TeachingAssignmentResponse> getAssignments(Long classroomId, Long teacherId) {
        Long schoolId = access.schoolScopeForCurrentUser();

        List<TeachingAssignment> assignments;
        if (classroomId != null) {
            Classroom classroom = loadAccessibleClassroom(classroomId);
            assignments = assignmentRepository.findByClassroomIdOrderBySubjectNameAsc(classroom.getId());
        } else if (teacherId != null) {
            Teacher teacher = loadAccessibleTeacher(teacherId);
            assignments = assignmentRepository.findByTeacherIdOrderByClassroomNameAsc(teacher.getId());
        } else {
            assignments = schoolId == null
                    ? assignmentRepository.findAllByOrderByClassroomNameAsc()
                    : assignmentRepository.findBySchoolIdOrderByClassroomNameAsc(schoolId);
        }

        return assignments.stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TeachingAssignmentResponse getAssignmentById(Long id) {
        return mapper.toResponse(loadAccessible(id));
    }

    @Override
    @Transactional
    public TeachingAssignmentResponse createAssignment(TeachingAssignmentRequest request) {
        Classroom classroom = loadAccessibleClassroom(request.classroomId());
        Long schoolId = classroom.getSchool().getId();

        Teacher teacher = loadAccessibleTeacher(request.teacherId());
        assertSameSchool(teacher, schoolId);
        Subject subject = resolveSubject(request.subjectId(), classroom);

        if (assignmentRepository.existsAssignment(teacher.getId(), classroom.getId(),
                subject != null ? subject.getId() : null, classroom.getAcademicYear().getId())) {
            throw new DuplicateResourceException(duplicateMessage(subject));
        }

        TeachingAssignment assignment = TeachingAssignment.builder()
                .teacher(teacher)
                .classroom(classroom)
                .subject(subject)
                .academicYear(classroom.getAcademicYear())
                .school(classroom.getSchool())
                .build();

        return mapper.toResponse(assignmentRepository.save(assignment));
    }

    @Override
    @Transactional
    public TeachingAssignmentResponse updateAssignment(Long id, TeachingAssignmentRequest request) {
        TeachingAssignment assignment = loadAccessible(id);
        Classroom classroom = loadAccessibleClassroom(request.classroomId());
        Long schoolId = classroom.getSchool().getId();

        Teacher teacher = loadAccessibleTeacher(request.teacherId());
        assertSameSchool(teacher, schoolId);
        Subject subject = resolveSubject(request.subjectId(), classroom);

        if (assignmentRepository.existsAssignmentExcluding(teacher.getId(), classroom.getId(),
                subject != null ? subject.getId() : null, classroom.getAcademicYear().getId(), id)) {
            throw new DuplicateResourceException(duplicateMessage(subject));
        }

        assignment.setTeacher(teacher);
        assignment.setClassroom(classroom);
        assignment.setSubject(subject);
        assignment.setAcademicYear(classroom.getAcademicYear());
        assignment.setSchool(classroom.getSchool());

        return mapper.toResponse(assignmentRepository.save(assignment));
    }

    @Override
    @Transactional
    public void deleteAssignment(Long id) {
        assignmentRepository.delete(loadAccessible(id));
    }

    // ------------------------------------------------------------------ helpers

    private TeachingAssignment loadAccessible(Long id) {
        TeachingAssignment assignment = assignmentRepository.findWithRelationsById(id)
                .orElseThrow(() -> NotFoundException.of("Teaching assignment", id));
        access.requireSchoolAccess(assignment.getSchool().getId());
        return assignment;
    }

    private Classroom loadAccessibleClassroom(Long classroomId) {
        Classroom classroom = classroomRepository.findWithRelationsById(classroomId)
                .orElseThrow(() -> NotFoundException.of("Classroom", classroomId));
        access.requireSchoolAccess(classroom.getSchool().getId());
        return classroom;
    }

    private Teacher loadAccessibleTeacher(Long teacherId) {
        Teacher teacher = teacherRepository.findWithRelationsById(teacherId)
                .orElseThrow(() -> NotFoundException.of("Teacher", teacherId));
        access.requireSchoolAccess(teacher.getSchool().getId());
        return teacher;
    }

    private void assertSameSchool(Teacher teacher, Long schoolId) {
        if (!teacher.getSchool().getId().equals(schoolId)) {
            throw new BadRequestException("The selected teacher belongs to a different school");
        }
    }

    /** The subject must sit under the classroom's grade, which also pins it to the school. */
    private Subject resolveSubject(Long subjectId, Classroom classroom) {
        if (subjectId == null) {
            return null;
        }
        Subject subject = subjectRepository.findWithRelationsById(subjectId)
                .orElseThrow(() -> NotFoundException.of("Subject", subjectId));
        access.requireSchoolAccess(subject.getSchool().getId());

        if (classroom.getGrade() == null
                || !subject.getGrade().getId().equals(classroom.getGrade().getId())) {
            throw new BadRequestException("The selected subject is not taught in this class's grade");
        }
        return subject;
    }

    private String duplicateMessage(Subject subject) {
        return subject == null
                ? "That teacher already has a whole-class assignment for this class"
                : "That teacher is already assigned to teach " + subject.getName() + " to this class";
    }
}
