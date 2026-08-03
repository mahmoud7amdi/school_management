package com.smartedu.school_management_api.mapper;

import com.smartedu.school_management_api.dto.common.ReferenceResponse;
import com.smartedu.school_management_api.dto.teaching.TeachingAssignmentResponse;
import com.smartedu.school_management_api.entity.Classroom;
import com.smartedu.school_management_api.entity.TeachingAssignment;
import org.springframework.stereotype.Component;

/**
 * Mapping for teaching assignments.
 *
 * <p>Must be called inside the service transaction: every association it reads is lazy,
 * and the repositories pre-load them via {@code @EntityGraph}.
 */
@Component
public class TeachingAssignmentMapper {

    public TeachingAssignmentResponse toResponse(TeachingAssignment assignment) {
        if (assignment == null) {
            return null;
        }

        Classroom classroom = assignment.getClassroom();
        // The grade is reported from the classroom rather than stored on the assignment,
        // so it cannot drift if the class is ever moved.
        var grade = classroom != null ? classroom.getGrade() : null;

        return new TeachingAssignmentResponse(
                assignment.getId(),
                assignment.getTeacher() != null
                        ? ReferenceResponse.of(assignment.getTeacher().getId(),
                        assignment.getTeacher().getFullName())
                        : null,
                classroom != null
                        ? ReferenceResponse.of(classroom.getId(), classroom.getName())
                        : null,
                assignment.getSubject() != null
                        ? ReferenceResponse.of(assignment.getSubject().getId(),
                        assignment.getSubject().getName())
                        : null,
                grade != null ? ReferenceResponse.of(grade.getId(), grade.getName()) : null,
                assignment.getAcademicYear() != null
                        ? ReferenceResponse.of(assignment.getAcademicYear().getId(),
                        assignment.getAcademicYear().getName())
                        : null,
                assignment.getSchool() != null
                        ? ReferenceResponse.of(assignment.getSchool().getId(),
                        assignment.getSchool().getName())
                        : null,
                assignment.getCreatedAt(),
                assignment.getUpdatedAt());
    }
}
