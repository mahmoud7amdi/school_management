package com.smartedu.school_management_api.service;

import com.smartedu.school_management_api.entity.School;
import java.util.List;
public interface SchoolService {
    School createSchool(School school);
    List<School> getAllSchools();
    School getSchoolById(Long id);
    School updateSchool(Long id, School schoolDetails);
    void deleteSchool(Long id);
}
