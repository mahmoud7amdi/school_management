package com.smartedu.school_management_api.dto.portal;

import com.smartedu.school_management_api.dto.common.ReferenceResponse;

import java.util.List;

/**
 * One of a teacher's classes, with the subjects they teach in it.
 *
 * <p>{@code subjects} comes from the teacher's own assignments, so two teachers looking at
 * the same class see different entries. {@code homeroom} marks the class they are the
 * class teacher of, which is a different relationship from an assignment.
 */
public record PortalClassResponse(
        Long id,
        String name,
        String roomNumber,
        Integer capacity,
        ReferenceResponse grade,
        ReferenceResponse section,
        ReferenceResponse academicYear,
        List<ReferenceResponse> subjects,
        boolean homeroom,
        long students
) {
}
