package com.smartedu.school_management_api.dto.teaching;

import jakarta.validation.constraints.NotNull;

/**
 * Payload for creating or updating a teaching assignment.
 *
 * <p>The school and academic year are both derived from the classroom rather than taken
 * from the body: the classroom already pins them, and re-accepting them would let a super
 * admin pair a class with another school's year.
 *
 * <p>{@code subjectId} is optional — null records a whole-class (homeroom) assignment.
 */
public record TeachingAssignmentRequest(
        @NotNull(message = "Teacher is required")
        Long teacherId,

        @NotNull(message = "Classroom is required")
        Long classroomId,

        Long subjectId
) {
}
