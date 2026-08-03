package com.smartedu.school_management_api.service;

import com.smartedu.school_management_api.dto.teaching.TeachingAssignmentRequest;
import com.smartedu.school_management_api.dto.teaching.TeachingAssignmentResponse;

import java.util.List;

public interface TeachingAssignmentService {

    /** All assignments in the caller's scope, optionally narrowed to one class or teacher. */
    List<TeachingAssignmentResponse> getAssignments(Long classroomId, Long teacherId);

    TeachingAssignmentResponse getAssignmentById(Long id);

    TeachingAssignmentResponse createAssignment(TeachingAssignmentRequest request);

    TeachingAssignmentResponse updateAssignment(Long id, TeachingAssignmentRequest request);

    void deleteAssignment(Long id);
}
