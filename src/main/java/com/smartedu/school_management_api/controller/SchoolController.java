package com.smartedu.school_management_api.controller;

import com.smartedu.school_management_api.dto.ApiResponse;
import com.smartedu.school_management_api.entity.School;
import com.smartedu.school_management_api.service.SchoolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/schools")
@RequiredArgsConstructor
public class SchoolController {

    private final SchoolService schoolService;

    // POST (Super Admin Only)
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<School>> createSchool(@Valid @RequestBody School school) {
        try {
            School created = schoolService.createSchool(school);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(HttpStatus.CREATED.value(), "School added successfully", created, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Failed to add school", null, e.getMessage()));
        }
    }

    // GET All (Super Admin Only)
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<School>>> getAllSchools() {
        try {
            List<School> schools = schoolService.getAllSchools();
            return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Fetched all schools successfully", schools, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error fetching schools", null, e.getMessage()));
        }
    }

    // GET by ID (Super Admin & School Admin)
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'SCHOOL_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<School>> getSchoolById(@PathVariable Long id) {
        try {
            School school = schoolService.getSchoolById(id);
            return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Fetched school successfully", school, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "School not found", null, e.getMessage()));
        }
    }

    // PUT (Super Admin Only)
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<School>> updateSchool(@PathVariable Long id, @RequestBody School school) {
        try {
            School updatedSchool = schoolService.updateSchool(id, school);
            return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "School updated successfully", updatedSchool, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Failed to update school", null, e.getMessage()));
        }
    }

    // DELETE (Super Admin Only)
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteSchool(@PathVariable Long id) {
        try {
            schoolService.deleteSchool(id);
            return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "School deleted successfully", "Deleted", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Failed to delete school", null, e.getMessage()));
        }
    }
}
