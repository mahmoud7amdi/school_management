package com.smartedu.school_management_api.controller;

import com.smartedu.school_management_api.dto.ApiResponse;
import com.smartedu.school_management_api.dto.parent.ParentRequest;
import com.smartedu.school_management_api.dto.parent.ParentResponse;
import com.smartedu.school_management_api.service.ParentService;
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

/**
 * Guardian administration. A parent's own view of their children lives on
 * {@link PortalController} instead.
 */
@RestController
@RequestMapping("/api/v1/parents")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'SCHOOL_ADMIN')")
public class ParentController {

    private final ParentService parentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ParentResponse>>> getAllParents() {
        return ResponseEntity.ok(ApiResponse.ok(parentService.getAllParents(), "Parents loaded"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ParentResponse>> getParentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(parentService.getParentById(id), "Parent loaded"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ParentResponse>> createParent(@Valid @RequestBody ParentRequest request) {
        ParentResponse created = parentService.createParent(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(created, "Parent added successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ParentResponse>> updateParent(@PathVariable Long id,
                                                                   @Valid @RequestBody ParentRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(parentService.updateParent(id, request),
                "Parent updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteParent(@PathVariable Long id) {
        parentService.deleteParent(id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", "Parent deleted successfully"));
    }
}
