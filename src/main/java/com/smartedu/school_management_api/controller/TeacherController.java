package com.smartedu.school_management_api.controller;

import com.smartedu.school_management_api.dto.ApiResponse;
import com.smartedu.school_management_api.dto.teacher.TeacherRequest;
import com.smartedu.school_management_api.dto.teacher.TeacherResponse;
import com.smartedu.school_management_api.service.TeacherService;
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
@RequestMapping("/api/v1/teachers")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SCHOOL_ADMIN')")
public class TeacherController {

    private final TeacherService teacherService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TeacherResponse>>> getAllTeachers() {
        return ResponseEntity.ok(ApiResponse.ok(teacherService.getAllTeachers(), "Teachers loaded"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TeacherResponse>> getTeacherById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(teacherService.getTeacherById(id), "Teacher loaded"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TeacherResponse>> createTeacher(@Valid @RequestBody TeacherRequest request) {
        TeacherResponse created = teacherService.createTeacher(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(created, "Teacher added successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TeacherResponse>> updateTeacher(@PathVariable Long id,
                                                                     @Valid @RequestBody TeacherRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(teacherService.updateTeacher(id, request),
                "Teacher updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteTeacher(@PathVariable Long id) {
        teacherService.deleteTeacher(id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", "Teacher deleted successfully"));
    }
}
