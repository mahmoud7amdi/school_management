package com.smartedu.school_management_api.mapper;

import com.smartedu.school_management_api.dto.common.ReferenceResponse;
import com.smartedu.school_management_api.dto.parent.ParentResponse;
import com.smartedu.school_management_api.entity.Parent;
import com.smartedu.school_management_api.entity.School;
import com.smartedu.school_management_api.entity.Student;
import com.smartedu.school_management_api.entity.StudentGuardian;
import org.springframework.stereotype.Component;

import java.util.List;

/** Mapping for guardians and the links to their children. */
@Component
public class ParentMapper {

    /**
     * {@code links} is supplied by the service rather than read off the entity, so the
     * mapper stays repository-free and the caller controls the fetch.
     */
    public ParentResponse toResponse(Parent parent, List<StudentGuardian> links) {
        if (parent == null) {
            return null;
        }
        return new ParentResponse(
                parent.getId(),
                parent.getFirstName(),
                parent.getLastName(),
                parent.getFullName(),
                parent.getEmail(),
                parent.getPhoneNumber(),
                parent.getOccupation(),
                parent.getAddress(),
                schoolRef(parent.getSchool()),
                parent.getUserAccount() != null,
                children(links),
                parent.getCreatedAt(),
                parent.getUpdatedAt());
    }

    public ParentResponse toResponse(Parent parent) {
        return toResponse(parent, List.of());
    }

    private List<ParentResponse.ChildResponse> children(List<StudentGuardian> links) {
        if (links == null || links.isEmpty()) {
            return List.of();
        }
        return links.stream().map(this::child).toList();
    }

    private ParentResponse.ChildResponse child(StudentGuardian link) {
        Student student = link.getStudent();
        return new ParentResponse.ChildResponse(
                student.getId(),
                student.getFullName(),
                student.getAdmissionNumber(),
                student.getGrade() != null
                        ? ReferenceResponse.of(student.getGrade().getId(), student.getGrade().getName())
                        : null,
                student.getClassroom() != null
                        ? ReferenceResponse.of(student.getClassroom().getId(), student.getClassroom().getName())
                        : null,
                link.getRelationship(),
                link.getRelationship() != null ? link.getRelationship().getLabel() : null,
                Boolean.TRUE.equals(link.getPrimaryContact()));
    }

    private ReferenceResponse schoolRef(School school) {
        return school != null ? ReferenceResponse.of(school.getId(), school.getName()) : null;
    }
}
