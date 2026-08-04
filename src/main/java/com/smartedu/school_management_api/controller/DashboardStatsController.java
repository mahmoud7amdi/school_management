package com.smartedu.school_management_api.controller;

import com.smartedu.school_management_api.dto.ApiResponse;
import com.smartedu.school_management_api.dto.dashboard.DashboardStatsResponse;
import com.smartedu.school_management_api.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Aggregate counts for the dashboard home, in a single request.
 *
 * <p>School admin only. A super admin's dashboard is served by
 * {@link ReportController} instead, because these figures are school-operational.
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SCHOOL_ADMIN')")
public class DashboardStatsController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getStats() {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getStats(), "Statistics loaded"));
    }
}
