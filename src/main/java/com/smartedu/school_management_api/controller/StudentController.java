package com.smartedu.school_management_api.controller;

import com.smartedu.school_management_api.dto.ApiResponse;
import com.smartedu.school_management_api.dto.common.PageResponse;
import com.smartedu.school_management_api.dto.student.StudentRequest;
import com.smartedu.school_management_api.dto.student.StudentResponse;
import com.smartedu.school_management_api.entity.StudentStatus;
import com.smartedu.school_management_api.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SCHOOL_ADMIN')")
public class StudentController {

    private static final int MAX_PAGE_SIZE = 100;

    private final StudentService studentService;

    /**
     * Paged, filtered listing used by the students table.
     *
     * <p>Page size is capped so a crafted {@code size} cannot pull the whole table.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<StudentResponse>>> searchStudents(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) StudentStatus status,
            @RequestParam(required = false) Long gradeId,
            @RequestParam(required = false) Long classroomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by("lastName").ascending().and(Sort.by("firstName").ascending()));

        PageResponse<StudentResponse> result =
                studentService.searchStudents(search, status, gradeId, classroomId, pageable);
        return ResponseEntity.ok(ApiResponse.ok(result, "Students loaded"));
    }

    /** Unpaged list, for dropdowns and exports. */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<StudentResponse>>> getAllStudents() {
        return ResponseEntity.ok(ApiResponse.ok(studentService.getAllStudents(), "Students loaded"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentResponse>> getStudentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(studentService.getStudentById(id), "Student loaded"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<StudentResponse>> createStudent(@Valid @RequestBody StudentRequest request) {
        StudentResponse created = studentService.createStudent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(created, "Student enrolled successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentResponse>> updateStudent(@PathVariable Long id,
                                                                    @Valid @RequestBody StudentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(studentService.updateStudent(id, request), "Student updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteStudent(@PathVariable Long id) {
        studentService.deleteStudent(id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", "Student deleted successfully"));
    }
}
