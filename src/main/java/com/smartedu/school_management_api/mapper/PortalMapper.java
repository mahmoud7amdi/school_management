package com.smartedu.school_management_api.mapper;

import com.smartedu.school_management_api.dto.common.ReferenceResponse;
import com.smartedu.school_management_api.dto.portal.AbsenceNoteResponse;
import com.smartedu.school_management_api.dto.portal.PortalClassResponse;
import com.smartedu.school_management_api.entity.AbsenceNote;
import com.smartedu.school_management_api.entity.Classroom;
import com.smartedu.school_management_api.entity.Student;
import com.smartedu.school_management_api.entity.Subject;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Collection;
import java.util.List;

/**
 * Mapping for the self-service portal.
 *
 * <p>Counts and the caller's own subject list are passed in by the service rather than
 * read off the entities, so the mapper stays repository-free — the convention the other
 * mappers here follow.
 */
@Component
public class PortalMapper {

    public PortalClassResponse toClassResponse(Classroom classroom,
                                               Collection<Subject> ownSubjects,
                                               boolean homeroom,
                                               long students) {
        if (classroom == null) {
            return null;
        }
        return new PortalClassResponse(
                classroom.getId(),
                classroom.getName(),
                classroom.getRoomNumber(),
                classroom.getCapacity(),
                classroom.getGrade() != null
                        ? ReferenceResponse.of(classroom.getGrade().getId(), classroom.getGrade().getName())
                        : null,
                classroom.getSection() != null
                        ? ReferenceResponse.of(classroom.getSection().getId(), classroom.getSection().getName())
                        : null,
                classroom.getAcademicYear() != null
                        ? ReferenceResponse.of(classroom.getAcademicYear().getId(),
                        classroom.getAcademicYear().getName())
                        : null,
                subjectRefs(ownSubjects),
                homeroom,
                students);
    }

    public AbsenceNoteResponse toResponse(AbsenceNote note) {
        if (note == null) {
            return null;
        }
        Student student = note.getStudent();
        Classroom classroom = student != null ? student.getClassroom() : null;

        return new AbsenceNoteResponse(
                note.getId(),
                note.getAbsenceDate(),
                note.getReason(),
                note.getStatus(),
                note.getStatus() != null ? note.getStatus().getLabel() : null,
                student != null ? ReferenceResponse.of(student.getId(), student.getFullName()) : null,
                student != null ? student.getAdmissionNumber() : null,
                classroom != null ? ReferenceResponse.of(classroom.getId(), classroom.getName()) : null,
                note.getSubmittedBy() != null
                        ? ReferenceResponse.of(null, note.getSubmittedBy().getFullName())
                        : null,
                note.getReviewedBy() != null
                        ? ReferenceResponse.of(null, note.getReviewedBy().getFullName())
                        : null,
                note.getReviewedAt(),
                note.getReviewNote(),
                note.getCreatedAt(),
                note.getUpdatedAt());
    }

    /** Sorted by name so the list is stable between requests. */
    private List<ReferenceResponse> subjectRefs(Collection<Subject> subjects) {
        if (subjects == null || subjects.isEmpty()) {
            return List.of();
        }
        return subjects.stream()
                .sorted(Comparator.comparing(Subject::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(subject -> ReferenceResponse.of(subject.getId(), subject.getName()))
                .toList();
    }
}
