package com.smartedu.school_management_api.controller;

import com.smartedu.school_management_api.dto.ApiResponse;
import com.smartedu.school_management_api.dto.section.SectionRequest;
import com.smartedu.school_management_api.dto.section.SectionResponse;
import com.smartedu.school_management_api.service.SectionService;
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
@RequestMapping("/api/v1/sections")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SCHOOL_ADMIN')")
public class SectionController {

    private final SectionService sectionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<SectionResponse>>> getAllSections() {
        return ResponseEntity.ok(ApiResponse.ok(sectionService.getAllSections(), "Sections loaded"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SectionResponse>> getSectionById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(sectionService.getSectionById(id), "Section loaded"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SectionResponse>> createSection(@Valid @RequestBody SectionRequest request) {
        SectionResponse created = sectionService.createSection(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(created, "Section added successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SectionResponse>> updateSection(@PathVariable Long id,
                                                                     @Valid @RequestBody SectionRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(sectionService.updateSection(id, request),
                "Section updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteSection(@PathVariable Long id) {
        sectionService.deleteSection(id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", "Section deleted successfully"));
    }
}
