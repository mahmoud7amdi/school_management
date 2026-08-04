package com.smartedu.school_management_api.controller;

import com.smartedu.school_management_api.dto.ApiResponse;
import com.smartedu.school_management_api.dto.grade.GradeRequest;
import com.smartedu.school_management_api.dto.grade.GradeResponse;
import com.smartedu.school_management_api.service.GradeService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/grades")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SCHOOL_ADMIN')")
public class GradeController {

    private final GradeService gradeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<GradeResponse>>> getAllGrades() {
        return ResponseEntity.ok(ApiResponse.ok(gradeService.getAllGrades(), "Grades loaded"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GradeResponse>> getGradeById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(gradeService.getGradeById(id), "Grade loaded"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<GradeResponse>> createGrade(@Valid @RequestBody GradeRequest request) {
        GradeResponse created = gradeService.createGrade(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(created, "Grade added successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GradeResponse>> updateGrade(@PathVariable Long id,
                                                                @Valid @RequestBody GradeRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(gradeService.updateGrade(id, request), "Grade updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteGrade(@PathVariable Long id) {
        gradeService.deleteGrade(id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", "Grade deleted successfully"));
    }
}
