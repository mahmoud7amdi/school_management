package com.smartedu.school_management_api.service;

import com.smartedu.school_management_api.dto.common.PageResponse;
import com.smartedu.school_management_api.dto.student.StudentRequest;
import com.smartedu.school_management_api.dto.student.StudentResponse;
import com.smartedu.school_management_api.entity.StudentStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface StudentService {

    List<StudentResponse> getAllStudents();

    /** Filtered, paged listing for the students table. Null filters are ignored. */
    PageResponse<StudentResponse> searchStudents(String search, StudentStatus status,
                                                 Long gradeId, Long classroomId, Pageable pageable);

    StudentResponse getStudentById(Long id);

    StudentResponse createStudent(StudentRequest request);

    StudentResponse updateStudent(Long id, StudentRequest request);

    void deleteStudent(Long id);
}
