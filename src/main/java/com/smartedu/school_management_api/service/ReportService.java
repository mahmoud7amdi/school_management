package com.smartedu.school_management_api.service;

import com.smartedu.school_management_api.dto.report.PlatformReportResponse;

public interface ReportService {

    /** The platform report: the caller's own profile, platform totals, per-school rows. */
    PlatformReportResponse getPlatformReport();
}
