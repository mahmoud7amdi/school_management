package com.smartedu.school_management_api.service;

import com.smartedu.school_management_api.dto.dashboard.DashboardStatsResponse;

public interface DashboardService {

    /** Aggregate counts for the dashboard home, scoped to the caller's school. */
    DashboardStatsResponse getStats();
}
