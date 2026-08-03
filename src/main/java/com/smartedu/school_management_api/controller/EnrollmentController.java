package com.smartedu.school_management_api.controller;

import com.smartedu.school_management_api.dto.ApiResponse;
import com.smartedu.school_management_api.dto.enrollment.EnrollmentRequest;
import com.smartedu.school_management_api.dto.enrollment.EnrollmentResponse;
import com.smartedu.school_management_api.service.EnrollmentService;
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

@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'SCHOOL_ADMIN')")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    /** Optionally narrowed to one student's history via {@code ?studentId=}. */
    @GetMapping
    public ResponseEntity<ApiResponse<List<EnrollmentResponse>>> getAllEnrollments(
            @RequestParam(required = false) Long studentId) {
        List<EnrollmentResponse> enrollments = studentId == null
                ? enrollmentService.getAllEnrollments()
                : enrollmentService.getEnrollmentsForStudent(studentId);
        return ResponseEntity.ok(ApiResponse.ok(enrollments, "Enrollments loaded"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> getEnrollmentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(enrollmentService.getEnrollmentById(id), "Enrollment loaded"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EnrollmentResponse>> createEnrollment(
            @Valid @RequestBody EnrollmentRequest request) {
        EnrollmentResponse created = enrollmentService.createEnrollment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(created, "Enrollment recorded successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EnrollmentResponse>> updateEnrollment(
            @PathVariable Long id, @Valid @RequestBody EnrollmentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(enrollmentService.updateEnrollment(id, request),
                "Enrollment updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteEnrollment(@PathVariable Long id) {
        enrollmentService.deleteEnrollment(id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", "Enrollment deleted successfully"));
    }
}
