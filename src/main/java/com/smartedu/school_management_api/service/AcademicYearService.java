package com.smartedu.school_management_api.service;

import com.smartedu.school_management_api.dto.academicyear.AcademicYearRequest;
import com.smartedu.school_management_api.dto.academicyear.AcademicYearResponse;

import java.util.List;

public interface AcademicYearService {

    List<AcademicYearResponse> getAllAcademicYears();

    AcademicYearResponse getAcademicYearById(Long id);

    AcademicYearResponse createAcademicYear(AcademicYearRequest request);

    AcademicYearResponse updateAcademicYear(Long id, AcademicYearRequest request);

    void deleteAcademicYear(Long id);
}
