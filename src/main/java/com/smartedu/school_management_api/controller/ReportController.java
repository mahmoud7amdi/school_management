package com.smartedu.school_management_api.controller;

import com.smartedu.school_management_api.dto.ApiResponse;
import com.smartedu.school_management_api.dto.report.PlatformReportResponse;
import com.smartedu.school_management_api.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Platform reporting for the super admin.
 *
 * <p>Restricted to {@code SUPER_ADMIN} rather than both admin tiers: a school admin's
 * reporting is its own dashboard, which is already scoped to its school.
 */
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SUPER_ADMIN')")
public class ReportController {

    private final ReportService reportService;

    /** The super admin's own profile, platform totals, and a row per school. */
    @GetMapping("/platform")
    public ResponseEntity<ApiResponse<PlatformReportResponse>> getPlatformReport() {
        return ResponseEntity.ok(ApiResponse.ok(reportService.getPlatformReport(), "Report loaded"));
    }
}
