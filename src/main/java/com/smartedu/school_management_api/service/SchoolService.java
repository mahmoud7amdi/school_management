package com.smartedu.school_management_api.service;

import com.smartedu.school_management_api.dto.school.SchoolRequest;
import com.smartedu.school_management_api.dto.school.SchoolResponse;

import java.util.List;

public interface SchoolService {

    SchoolResponse createSchool(SchoolRequest request);

    List<SchoolResponse> getAllSchools();

    SchoolResponse getSchoolById(Long id);

    SchoolResponse updateSchool(Long id, SchoolRequest request);

    void deleteSchool(Long id);
}
