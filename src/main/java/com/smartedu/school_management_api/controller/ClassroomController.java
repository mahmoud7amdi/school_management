package com.smartedu.school_management_api.controller;

import com.smartedu.school_management_api.dto.ApiResponse;
import com.smartedu.school_management_api.dto.classroom.ClassroomRequest;
import com.smartedu.school_management_api.dto.classroom.ClassroomResponse;
import com.smartedu.school_management_api.service.ClassroomService;
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
@RequestMapping("/api/v1/classrooms")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'SCHOOL_ADMIN')")
public class ClassroomController {

    private final ClassroomService classroomService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ClassroomResponse>>> getAllClassrooms() {
        return ResponseEntity.ok(ApiResponse.ok(classroomService.getAllClassrooms(), "Classrooms loaded"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClassroomResponse>> getClassroomById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(classroomService.getClassroomById(id), "Classroom loaded"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ClassroomResponse>> createClassroom(
            @Valid @RequestBody ClassroomRequest request) {
        ClassroomResponse created = classroomService.createClassroom(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(created, "Classroom added successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClassroomResponse>> updateClassroom(
            @PathVariable Long id, @Valid @RequestBody ClassroomRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                classroomService.updateClassroom(id, request), "Classroom updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteClassroom(@PathVariable Long id) {
        classroomService.deleteClassroom(id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", "Classroom deleted successfully"));
    }
}
