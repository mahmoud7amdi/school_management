package com.smartedu.school_management_api.controller;

import com.smartedu.school_management_api.dto.ApiResponse;
import com.smartedu.school_management_api.dto.academicyear.AcademicYearRequest;
import com.smartedu.school_management_api.dto.academicyear.AcademicYearResponse;
import com.smartedu.school_management_api.service.AcademicYearService;
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
@RequestMapping("/api/v1/academic-years")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SCHOOL_ADMIN')")
public class AcademicYearController {

    private final AcademicYearService academicYearService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AcademicYearResponse>>> getAllAcademicYears() {
        return ResponseEntity.ok(ApiResponse.ok(academicYearService.getAllAcademicYears(), "Academic years loaded"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AcademicYearResponse>> getAcademicYearById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(academicYearService.getAcademicYearById(id), "Academic year loaded"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AcademicYearResponse>> createAcademicYear(
            @Valid @RequestBody AcademicYearRequest request) {
        AcademicYearResponse created = academicYearService.createAcademicYear(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(created, "Academic year added successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AcademicYearResponse>> updateAcademicYear(
            @PathVariable Long id, @Valid @RequestBody AcademicYearRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                academicYearService.updateAcademicYear(id, request), "Academic year updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteAcademicYear(@PathVariable Long id) {
        academicYearService.deleteAcademicYear(id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", "Academic year deleted successfully"));
    }
}
