package com.smartedu.school_management_api.service.impl;

import com.smartedu.school_management_api.dto.section.SectionRequest;
import com.smartedu.school_management_api.dto.section.SectionResponse;
import com.smartedu.school_management_api.entity.Grade;
import com.smartedu.school_management_api.entity.Section;
import com.smartedu.school_management_api.entity.Teacher;
import com.smartedu.school_management_api.exception.BadRequestException;
import com.smartedu.school_management_api.exception.DuplicateResourceException;
import com.smartedu.school_management_api.exception.NotFoundException;
import com.smartedu.school_management_api.mapper.StaffMapper;
import com.smartedu.school_management_api.repository.ClassroomRepository;
import com.smartedu.school_management_api.repository.GradeRepository;
import com.smartedu.school_management_api.repository.SectionRepository;
import com.smartedu.school_management_api.repository.TeacherRepository;
import com.smartedu.school_management_api.service.SchoolAccessService;
import com.smartedu.school_management_api.service.SectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SectionServiceImpl implements SectionService {

    private final SectionRepository sectionRepository;
    private final GradeRepository gradeRepository;
    private final TeacherRepository teacherRepository;
    private final ClassroomRepository classroomRepository;
    private final SchoolAccessService access;
    private final StaffMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<SectionResponse> getAllSections() {
        Long schoolId = access.schoolScopeForCurrentUser();
        List<Section> sections = schoolId == null
                ? sectionRepository.findAllByOrderByGradeLevelOrderAscNameAsc()
                : sectionRepository.findBySchoolIdOrderByGradeLevelOrderAscNameAsc(schoolId);

        return sections.stream()
                .map(section -> mapper.toResponse(section, classroomRepository.countBySectionId(section.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SectionResponse getSectionById(Long id) {
        Section section = loadAccessible(id);
        return mapper.toResponse(section, classroomRepository.countBySectionId(id));
    }

    @Override
    @Transactional
    public SectionResponse createSection(SectionRequest request) {
        Grade grade = loadAccessibleGrade(request.gradeId());
        String name = request.name().trim();

        if (sectionRepository.existsByGradeIdAndNameIgnoreCase(grade.getId(), name)) {
            throw new DuplicateResourceException("A section named '" + name + "' already exists for this grade");
        }

        Section section = Section.builder()
                .name(name)
                .capacity(request.capacity())
                .description(trimToNull(request.description()))
                .grade(grade)
                .sectionHead(resolveSectionHead(request.sectionHeadId(), grade.getSchool().getId()))
                .school(grade.getSchool())
                .build();

        return mapper.toResponse(sectionRepository.save(section), 0L);
    }

    @Override
    @Transactional
    public SectionResponse updateSection(Long id, SectionRequest request) {
        Section section = loadAccessible(id);
        Grade grade = loadAccessibleGrade(request.gradeId());
        String name = request.name().trim();

        if (sectionRepository.existsByGradeIdAndNameIgnoreCaseAndIdNot(grade.getId(), name, id)) {
            throw new DuplicateResourceException("A section named '" + name + "' already exists for this grade");
        }

        section.setName(name);
        section.setCapacity(request.capacity());
        section.setDescription(trimToNull(request.description()));
        section.setGrade(grade);
        section.setSectionHead(resolveSectionHead(request.sectionHeadId(), grade.getSchool().getId()));
        section.setSchool(grade.getSchool());

        return mapper.toResponse(sectionRepository.save(section), classroomRepository.countBySectionId(id));
    }

    @Override
    @Transactional
    public void deleteSection(Long id) {
        Section section = loadAccessible(id);

        long classroomCount = classroomRepository.countBySectionId(id);
        if (classroomCount > 0) {
            throw new BadRequestException("This section is used by " + classroomCount
                    + " classroom(s). Remove the section from those classrooms before deleting it.");
        }
        sectionRepository.delete(section);
    }

    // ------------------------------------------------------------------ helpers

    private Section loadAccessible(Long id) {
        Section section = sectionRepository.findWithRelationsById(id)
                .orElseThrow(() -> NotFoundException.of("Section", id));
        access.requireSchoolAccess(section.getSchool().getId());
        return section;
    }

    private Grade loadAccessibleGrade(Long gradeId) {
        Grade grade = gradeRepository.findWithSchoolById(gradeId)
                .orElseThrow(() -> NotFoundException.of("Grade", gradeId));
        access.requireSchoolAccess(grade.getSchool().getId());
        return grade;
    }

    private Teacher resolveSectionHead(Long teacherId, Long schoolId) {
        if (teacherId == null) {
            return null;
        }
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> NotFoundException.of("Teacher", teacherId));
        if (!teacher.getSchool().getId().equals(schoolId)) {
            throw new BadRequestException("The selected teacher belongs to a different school");
        }
        return teacher;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
