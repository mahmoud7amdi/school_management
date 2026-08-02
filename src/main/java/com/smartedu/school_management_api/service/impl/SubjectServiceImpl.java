package com.smartedu.school_management_api.service.impl;

import com.smartedu.school_management_api.dto.subject.SubjectRequest;
import com.smartedu.school_management_api.dto.subject.SubjectResponse;
import com.smartedu.school_management_api.entity.Grade;
import com.smartedu.school_management_api.entity.Subject;
import com.smartedu.school_management_api.exception.DuplicateResourceException;
import com.smartedu.school_management_api.exception.NotFoundException;
import com.smartedu.school_management_api.mapper.AcademicMapper;
import com.smartedu.school_management_api.repository.GradeRepository;
import com.smartedu.school_management_api.repository.SubjectRepository;
import com.smartedu.school_management_api.service.SchoolAccessService;
import com.smartedu.school_management_api.service.SubjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final GradeRepository gradeRepository;
    private final SchoolAccessService access;
    private final AcademicMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<SubjectResponse> getAllSubjects() {
        Long schoolId = access.schoolScopeForCurrentUser();
        List<Subject> subjects = schoolId == null
                ? subjectRepository.findAllByOrderByNameAsc()
                : subjectRepository.findBySchoolIdOrderByNameAsc(schoolId);
        return subjects.stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SubjectResponse getSubjectById(Long id) {
        return mapper.toResponse(loadAccessible(id));
    }

    @Override
    @Transactional
    public SubjectResponse createSubject(SubjectRequest request) {
        // The grade determines the school, so there is no way to attach a subject to a
        // school the caller cannot reach.
        Grade grade = loadAccessibleGrade(request.gradeId());
        String name = request.name().trim();

        if (subjectRepository.existsByGradeIdAndNameIgnoreCase(grade.getId(), name)) {
            throw new DuplicateResourceException("A subject named '" + name + "' already exists for this grade");
        }

        Subject subject = Subject.builder()
                .name(name)
                .code(trimToNull(request.code()))
                .weeklyHours(request.weeklyHours())
                .grade(grade)
                .school(grade.getSchool())
                .build();

        return mapper.toResponse(subjectRepository.save(subject));
    }

    @Override
    @Transactional
    public SubjectResponse updateSubject(Long id, SubjectRequest request) {
        Subject subject = loadAccessible(id);
        Grade grade = loadAccessibleGrade(request.gradeId());
        String name = request.name().trim();

        if (subjectRepository.existsByGradeIdAndNameIgnoreCaseAndIdNot(grade.getId(), name, id)) {
            throw new DuplicateResourceException("A subject named '" + name + "' already exists for this grade");
        }

        subject.setName(name);
        subject.setCode(trimToNull(request.code()));
        subject.setWeeklyHours(request.weeklyHours());
        subject.setGrade(grade);
        subject.setSchool(grade.getSchool());

        return mapper.toResponse(subjectRepository.save(subject));
    }

    @Override
    @Transactional
    public void deleteSubject(Long id) {
        subjectRepository.delete(loadAccessible(id));
    }

    private Subject loadAccessible(Long id) {
        Subject subject = subjectRepository.findWithRelationsById(id)
                .orElseThrow(() -> NotFoundException.of("Subject", id));
        access.requireSchoolAccess(subject.getSchool().getId());
        return subject;
    }

    private Grade loadAccessibleGrade(Long gradeId) {
        Grade grade = gradeRepository.findWithSchoolById(gradeId)
                .orElseThrow(() -> NotFoundException.of("Grade", gradeId));
        access.requireSchoolAccess(grade.getSchool().getId());
        return grade;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
