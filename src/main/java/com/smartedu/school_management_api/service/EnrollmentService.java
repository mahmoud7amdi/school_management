package com.smartedu.school_management_api.service;

import com.smartedu.school_management_api.dto.enrollment.EnrollmentRequest;
import com.smartedu.school_management_api.dto.enrollment.EnrollmentResponse;

import java.util.List;

public interface EnrollmentService {

    List<EnrollmentResponse> getAllEnrollments();

    EnrollmentResponse getEnrollmentById(Long id);

    /** A student's enrolment history, most recent year first. */
    List<EnrollmentResponse> getEnrollmentsForStudent(Long studentId);

    EnrollmentResponse createEnrollment(EnrollmentRequest request);

    EnrollmentResponse updateEnrollment(Long id, EnrollmentRequest request);

    void deleteEnrollment(Long id);
}
