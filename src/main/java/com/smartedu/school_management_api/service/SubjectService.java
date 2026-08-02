package com.smartedu.school_management_api.service;

import com.smartedu.school_management_api.dto.subject.SubjectRequest;
import com.smartedu.school_management_api.dto.subject.SubjectResponse;

import java.util.List;

public interface SubjectService {

    List<SubjectResponse> getAllSubjects();

    SubjectResponse getSubjectById(Long id);

    SubjectResponse createSubject(SubjectRequest request);

    SubjectResponse updateSubject(Long id, SubjectRequest request);

    void deleteSubject(Long id);
}
