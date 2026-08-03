package com.smartedu.school_management_api.controller;

import com.smartedu.school_management_api.dto.ApiResponse;
import com.smartedu.school_management_api.dto.teaching.TeachingAssignmentRequest;
import com.smartedu.school_management_api.dto.teaching.TeachingAssignmentResponse;
import com.smartedu.school_management_api.service.TeachingAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Teaching assignments — the authority for what a teacher may see in the portal.
 *
 * <p>Administered here; a teacher reads their own through {@link PortalController}.
 */
@RestController
@RequestMapping("/api/v1/teaching-assignments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'SCHOOL_ADMIN')")
public class TeachingAssignmentController {

    private final TeachingAssignmentService assignmentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TeachingAssignmentResponse>>> getAssignments(
            @RequestParam(required = false) Long classroomId,
            @RequestParam(required = false) Long teacherId) {
        return ResponseEntity.ok(ApiResponse.ok(assignmentService.getAssignments(classroomId, teacherId),
                "Assignments loaded"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TeachingAssignmentResponse>> getAssignmentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(assignmentService.getAssignmentById(id), "Assignment loaded"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TeachingAssignmentResponse>> createAssignment(
            @Valid @RequestBody TeachingAssignmentRequest request) {
        TeachingAssignmentResponse created = assignmentService.createAssignment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(created, "Assignment added successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TeachingAssignmentResponse>> updateAssignment(
            @PathVariable Long id, @Valid @RequestBody TeachingAssignmentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(assignmentService.updateAssignment(id, request),
                "Assignment updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteAssignment(@PathVariable Long id) {
        assignmentService.deleteAssignment(id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", "Assignment removed successfully"));
    }
}
