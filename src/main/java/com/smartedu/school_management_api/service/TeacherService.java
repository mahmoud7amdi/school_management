package com.smartedu.school_management_api.service;

import com.smartedu.school_management_api.dto.teacher.TeacherRequest;
import com.smartedu.school_management_api.dto.teacher.TeacherResponse;

import java.util.List;

public interface TeacherService {

    List<TeacherResponse> getAllTeachers();

    TeacherResponse getTeacherById(Long id);

    TeacherResponse createTeacher(TeacherRequest request);

    TeacherResponse updateTeacher(Long id, TeacherRequest request);

    void deleteTeacher(Long id);
}
