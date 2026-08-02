package com.smartedu.school_management_api.controller;

import com.smartedu.school_management_api.dto.ApiResponse;
import com.smartedu.school_management_api.dto.school.SchoolRequest;
import com.smartedu.school_management_api.dto.school.SchoolResponse;
import com.smartedu.school_management_api.service.SchoolService;
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
@RequestMapping("/api/v1/schools")
@RequiredArgsConstructor
public class SchoolController {

    private final SchoolService schoolService;

    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<SchoolResponse>> createSchool(@Valid @RequestBody SchoolRequest request) {
        SchoolResponse created = schoolService.createSchool(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(created, "School added successfully"));
    }

    /**
     * A school admin gets a single-element list containing only its own school, which
     * the add-user and student forms use to populate their school field.
     */
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'SCHOOL_ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<SchoolResponse>>> getAllSchools() {
        return ResponseEntity.ok(ApiResponse.ok(schoolService.getAllSchools(), "Schools loaded"));
    }

    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'SCHOOL_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SchoolResponse>> getSchoolById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(schoolService.getSchoolById(id), "School loaded"));
    }

    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SchoolResponse>> updateSchool(@PathVariable Long id,
                                                                  @Valid @RequestBody SchoolRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(schoolService.updateSchool(id, request), "School updated successfully"));
    }

    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteSchool(@PathVariable Long id) {
        schoolService.deleteSchool(id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", "School deleted successfully"));
    }
}
