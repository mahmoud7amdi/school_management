package com.smartedu.school_management_api.service;

import com.smartedu.school_management_api.dto.grade.GradeRequest;
import com.smartedu.school_management_api.dto.grade.GradeResponse;

import java.util.List;

public interface GradeService {

    List<GradeResponse> getAllGrades();

    GradeResponse getGradeById(Long id);

    GradeResponse createGrade(GradeRequest request);

    GradeResponse updateGrade(Long id, GradeRequest request);

    void deleteGrade(Long id);
}
