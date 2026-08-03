package com.smartedu.school_management_api.mapper;

import com.smartedu.school_management_api.dto.classroom.ClassroomResponse;
import com.smartedu.school_management_api.dto.common.ReferenceResponse;
import com.smartedu.school_management_api.entity.AcademicYear;
import com.smartedu.school_management_api.entity.Classroom;
import com.smartedu.school_management_api.entity.Grade;
import com.smartedu.school_management_api.entity.School;
import com.smartedu.school_management_api.entity.Section;
import com.smartedu.school_management_api.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ClassroomMapper {

    /** {@code studentCount} is passed in so the mapper never issues its own queries. */
    public ClassroomResponse toResponse(Classroom classroom, long studentCount) {
        if (classroom == null) {
            return null;
        }
        Grade grade = classroom.getGrade();
        AcademicYear year = classroom.getAcademicYear();
        Section section = classroom.getSection();
        User teacher = classroom.getClassTeacher();
        School school = classroom.getSchool();

        return new ClassroomResponse(
                classroom.getId(),
                classroom.getName(),
                classroom.getCapacity(),
                classroom.getRoomNumber(),
                grade != null ? ReferenceResponse.of(grade.getId(), grade.getName()) : null,
                year != null ? ReferenceResponse.of(year.getId(), year.getName()) : null,
                section != null ? ReferenceResponse.of(section.getId(), section.getName()) : null,
                teacher != null ? teacher.getFullName() : null,
                school != null ? ReferenceResponse.of(school.getId(), school.getName()) : null,
                studentCount,
                classroom.getCreatedAt(),
                classroom.getUpdatedAt());
    }
}
