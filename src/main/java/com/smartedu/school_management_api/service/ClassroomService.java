package com.smartedu.school_management_api.service;

import com.smartedu.school_management_api.dto.classroom.ClassroomRequest;
import com.smartedu.school_management_api.dto.classroom.ClassroomResponse;

import java.util.List;

public interface ClassroomService {

    List<ClassroomResponse> getAllClassrooms();

    ClassroomResponse getClassroomById(Long id);

    ClassroomResponse createClassroom(ClassroomRequest request);

    ClassroomResponse updateClassroom(Long id, ClassroomRequest request);

    void deleteClassroom(Long id);
}
