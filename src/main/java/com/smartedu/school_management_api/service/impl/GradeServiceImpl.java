package com.smartedu.school_management_api.service.impl;

import com.smartedu.school_management_api.dto.grade.GradeRequest;
import com.smartedu.school_management_api.dto.grade.GradeResponse;
import com.smartedu.school_management_api.entity.Grade;
import com.smartedu.school_management_api.entity.School;
import com.smartedu.school_management_api.exception.BadRequestException;
import com.smartedu.school_management_api.exception.DuplicateResourceException;
import com.smartedu.school_management_api.exception.NotFoundException;
import com.smartedu.school_management_api.mapper.AcademicMapper;
import com.smartedu.school_management_api.repository.GradeRepository;
import com.smartedu.school_management_api.repository.StudentRepository;
import com.smartedu.school_management_api.service.GradeService;
import com.smartedu.school_management_api.service.SchoolAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GradeServiceImpl implements GradeService {

    private final GradeRepository gradeRepository;
    private final StudentRepository studentRepository;
    private final SchoolAccessService access;
    private final AcademicMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<GradeResponse> getAllGrades() {
        Long schoolId = access.schoolScopeForCurrentUser();
        List<Grade> grades = schoolId == null
                ? gradeRepository.findAllByOrderByLevelOrderAscNameAsc()
                : gradeRepository.findBySchoolIdOrderByLevelOrderAscNameAsc(schoolId);

        return grades.stream()
                .map(grade -> mapper.toResponse(grade, studentRepository.countByGradeId(grade.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GradeResponse getGradeById(Long id) {
        Grade grade = loadAccessible(id);
        return mapper.toResponse(grade, studentRepository.countByGradeId(id));
    }

    @Override
    @Transactional
    public GradeResponse createGrade(GradeRequest request) {
        School school = access.resolveWritableSchool(request.schoolId());
        String name = request.name().trim();

        if (gradeRepository.existsBySchoolIdAndNameIgnoreCase(school.getId(), name)) {
            throw new DuplicateResourceException("A grade named '" + name + "' already exists for this school");
        }

        Grade grade = Grade.builder()
                .name(name)
                .levelOrder(request.levelOrder())
                .description(trimToNull(request.description()))
                .school(school)
                .build();

        return mapper.toResponse(gradeRepository.save(grade), 0L);
    }

    @Override
    @Transactional
    public GradeResponse updateGrade(Long id, GradeRequest request) {
        Grade grade = loadAccessible(id);
        String name = request.name().trim();
        Long schoolId = grade.getSchool().getId();

        if (gradeRepository.existsBySchoolIdAndNameIgnoreCaseAndIdNot(schoolId, name, id)) {
            throw new DuplicateResourceException("A grade named '" + name + "' already exists for this school");
        }

        grade.setName(name);
        grade.setLevelOrder(request.levelOrder());
        grade.setDescription(trimToNull(request.description()));

        return mapper.toResponse(gradeRepository.save(grade), studentRepository.countByGradeId(id));
    }

    @Override
    @Transactional
    public void deleteGrade(Long id) {
        Grade grade = loadAccessible(id);

        // Students have a non-null grade, so removing one would orphan them.
        long studentCount = studentRepository.countByGradeId(id);
        if (studentCount > 0) {
            throw new BadRequestException("This grade has " + studentCount
                    + " student(s). Move them to another grade before deleting it.");
        }
        gradeRepository.delete(grade);
    }

    private Grade loadAccessible(Long id) {
        Grade grade = gradeRepository.findWithSchoolById(id)
                .orElseThrow(() -> NotFoundException.of("Grade", id));
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
