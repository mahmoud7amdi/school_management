package com.smartedu.school_management_api.mapper;

import com.smartedu.school_management_api.dto.common.ReferenceResponse;
import com.smartedu.school_management_api.dto.section.SectionResponse;
import com.smartedu.school_management_api.dto.teacher.TeacherResponse;
import com.smartedu.school_management_api.entity.Grade;
import com.smartedu.school_management_api.entity.School;
import com.smartedu.school_management_api.entity.Section;
import com.smartedu.school_management_api.entity.Subject;
import com.smartedu.school_management_api.entity.Teacher;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

/** Mapping for staff and the grade divisions they head. */
@Component
public class StaffMapper {

    public TeacherResponse toResponse(Teacher teacher) {
        if (teacher == null) {
            return null;
        }
        return new TeacherResponse(
                teacher.getId(),
                teacher.getEmployeeNumber(),
                teacher.getFirstName(),
                teacher.getLastName(),
                teacher.getFullName(),
                teacher.getGender(),
                teacher.getDateOfBirth(),
                teacher.getEmail(),
                teacher.getPhoneNumber(),
                teacher.getAddress(),
                teacher.getQualification(),
                teacher.getSpecialization(),
                teacher.getHireDate(),
                teacher.getStatus(),
                teacher.getStatus() != null ? teacher.getStatus().getLabel() : null,
                subjectRefs(teacher.getSubjects()),
                schoolRef(teacher.getSchool()),
                teacher.getUserAccount() != null,
                teacher.getCreatedAt(),
                teacher.getUpdatedAt());
    }

    /** {@code classroomCount} is supplied by the service so the mapper stays repository-free. */
    public SectionResponse toResponse(Section section, long classroomCount) {
        if (section == null) {
            return null;
        }
        Grade grade = section.getGrade();
        Teacher head = section.getSectionHead();

        return new SectionResponse(
                section.getId(),
                section.getName(),
                section.getCapacity(),
                section.getDescription(),
                grade != null ? ReferenceResponse.of(grade.getId(), grade.getName()) : null,
                schoolRef(section.getSchool()),
                head != null ? ReferenceResponse.of(head.getId(), head.getFullName()) : null,
                classroomCount,
                section.getCreatedAt(),
                section.getUpdatedAt());
    }

    /** Sorted by name so the list is stable between requests. */
    private List<ReferenceResponse> subjectRefs(Set<Subject> subjects) {
        if (subjects == null || subjects.isEmpty()) {
            return List.of();
        }
        return subjects.stream()
                .sorted(Comparator.comparing(Subject::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(subject -> ReferenceResponse.of(subject.getId(), subject.getName()))
                .toList();
    }

    private ReferenceResponse schoolRef(School school) {
        return school != null ? ReferenceResponse.of(school.getId(), school.getName()) : null;
    }
}
