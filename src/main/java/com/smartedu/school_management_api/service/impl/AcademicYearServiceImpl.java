package com.smartedu.school_management_api.service.impl;

import com.smartedu.school_management_api.dto.academicyear.AcademicYearRequest;
import com.smartedu.school_management_api.dto.academicyear.AcademicYearResponse;
import com.smartedu.school_management_api.entity.AcademicYear;
import com.smartedu.school_management_api.entity.School;
import com.smartedu.school_management_api.exception.BadRequestException;
import com.smartedu.school_management_api.exception.DuplicateResourceException;
import com.smartedu.school_management_api.exception.NotFoundException;
import com.smartedu.school_management_api.mapper.AcademicMapper;
import com.smartedu.school_management_api.repository.AcademicYearRepository;
import com.smartedu.school_management_api.repository.ClassroomRepository;
import com.smartedu.school_management_api.service.AcademicYearService;
import com.smartedu.school_management_api.service.SchoolAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AcademicYearServiceImpl implements AcademicYearService {

    private final AcademicYearRepository academicYearRepository;
    private final ClassroomRepository classroomRepository;
    private final SchoolAccessService access;
    private final AcademicMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<AcademicYearResponse> getAllAcademicYears() {
        Long schoolId = access.schoolScopeForCurrentUser();
        List<AcademicYear> years = schoolId == null
                ? academicYearRepository.findAllByOrderByStartDateDesc()
                : academicYearRepository.findBySchoolIdOrderByStartDateDesc(schoolId);
        return years.stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AcademicYearResponse getAcademicYearById(Long id) {
        return mapper.toResponse(loadAccessible(id));
    }

    @Override
    @Transactional
    public AcademicYearResponse createAcademicYear(AcademicYearRequest request) {
        School school = access.resolveWritableSchool(request.schoolId());
        validateDates(request);

        String name = request.name().trim();
        if (academicYearRepository.existsBySchoolIdAndNameIgnoreCase(school.getId(), name)) {
            throw new DuplicateResourceException("An academic year named '" + name + "' already exists for this school");
        }

        AcademicYear year = AcademicYear.builder()
                .name(name)
                .startDate(request.startDate())
                .endDate(request.endDate())
                .current(Boolean.TRUE.equals(request.current()))
                .school(school)
                .build();

        AcademicYear saved = academicYearRepository.save(year);
        if (Boolean.TRUE.equals(saved.getCurrent())) {
            academicYearRepository.clearCurrentFlagExcept(school.getId(), saved.getId());
        }
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AcademicYearResponse updateAcademicYear(Long id, AcademicYearRequest request) {
        AcademicYear year = loadAccessible(id);
        validateDates(request);

        String name = request.name().trim();
        Long schoolId = year.getSchool().getId();
        if (academicYearRepository.existsBySchoolIdAndNameIgnoreCaseAndIdNot(schoolId, name, id)) {
            throw new DuplicateResourceException("An academic year named '" + name + "' already exists for this school");
        }

        year.setName(name);
        year.setStartDate(request.startDate());
        year.setEndDate(request.endDate());
        if (request.current() != null) {
            year.setCurrent(request.current());
        }

        AcademicYear saved = academicYearRepository.save(year);
        if (Boolean.TRUE.equals(saved.getCurrent())) {
            academicYearRepository.clearCurrentFlagExcept(schoolId, saved.getId());
        }
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteAcademicYear(Long id) {
        AcademicYear year = loadAccessible(id);

        // Classrooms cascade from the year, and students hang off classrooms. Refuse
        // rather than silently wiping a term's worth of class groups.
        long classroomCount = year.getClassrooms() == null ? 0 : year.getClassrooms().size();
        if (classroomCount > 0) {
            throw new BadRequestException("This academic year has " + classroomCount
                    + " classroom(s). Delete or move them before deleting the year.");
        }
        academicYearRepository.delete(year);
    }

    private AcademicYear loadAccessible(Long id) {
        AcademicYear year = academicYearRepository.findWithSchoolById(id)
                .orElseThrow(() -> NotFoundException.of("Academic year", id));
        access.requireSchoolAccess(year.getSchool().getId());
        return year;
    }

    private void validateDates(AcademicYearRequest request) {
        if (!request.endDate().isAfter(request.startDate())) {
            throw new BadRequestException("The end date must be after the start date");
        }
    }
}
