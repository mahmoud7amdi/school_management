package com.smartedu.school_management_api.mapper;

import com.smartedu.school_management_api.dto.academicyear.AcademicYearResponse;
import com.smartedu.school_management_api.dto.common.ReferenceResponse;
import com.smartedu.school_management_api.dto.grade.GradeResponse;
import com.smartedu.school_management_api.dto.subject.SubjectResponse;
import com.smartedu.school_management_api.entity.AcademicYear;
import com.smartedu.school_management_api.entity.Grade;
import com.smartedu.school_management_api.entity.School;
import com.smartedu.school_management_api.entity.Subject;
import org.springframework.stereotype.Component;

/** Mapping for the academic reference data: years, grades and subjects. */
@Component
public class AcademicMapper {

    public AcademicYearResponse toResponse(AcademicYear year) {
        if (year == null) {
            return null;
        }
        return new AcademicYearResponse(
                year.getId(),
                year.getName(),
                year.getStartDate(),
                year.getEndDate(),
                year.getCurrent(),
                schoolRef(year.getSchool()),
                year.getCreatedAt(),
                year.getUpdatedAt());
    }

    /** {@code studentCount} is supplied by the service so the mapper stays repository-free. */
    public GradeResponse toResponse(Grade grade, long studentCount) {
        if (grade == null) {
            return null;
        }
        return new GradeResponse(
                grade.getId(),
                grade.getName(),
                grade.getLevelOrder(),
                grade.getDescription(),
                schoolRef(grade.getSchool()),
                studentCount,
                grade.getCreatedAt(),
                grade.getUpdatedAt());
    }

    public SubjectResponse toResponse(Subject subject) {
        if (subject == null) {
            return null;
        }
        Grade grade = subject.getGrade();
        return new SubjectResponse(
                subject.getId(),
                subject.getName(),
                subject.getCode(),
                subject.getWeeklyHours(),
                grade != null ? ReferenceResponse.of(grade.getId(), grade.getName()) : null,
                schoolRef(subject.getSchool()),
                subject.getCreatedAt(),
                subject.getUpdatedAt());
    }

    private ReferenceResponse schoolRef(School school) {
        return school != null ? ReferenceResponse.of(school.getId(), school.getName()) : null;
    }
}
