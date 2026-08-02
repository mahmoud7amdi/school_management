package com.smartedu.school_management_api.mapper;

import com.smartedu.school_management_api.dto.common.ReferenceResponse;
import com.smartedu.school_management_api.dto.student.StudentResponse;
import com.smartedu.school_management_api.entity.Classroom;
import com.smartedu.school_management_api.entity.Grade;
import com.smartedu.school_management_api.entity.School;
import com.smartedu.school_management_api.entity.Student;
import org.springframework.stereotype.Component;

@Component
public class StudentMapper {

    public StudentResponse toResponse(Student student) {
        if (student == null) {
            return null;
        }
        Grade grade = student.getGrade();
        Classroom classroom = student.getClassroom();
        School school = student.getSchool();

        return new StudentResponse(
                student.getId(),
                student.getAdmissionNumber(),
                student.getFirstName(),
                student.getLastName(),
                student.getFullName(),
                student.getGender(),
                student.getDateOfBirth(),
                student.getAge(),
                student.getEmail(),
                student.getPhoneNumber(),
                student.getAddress(),
                student.getPhotoUrl(),
                student.getGuardianName(),
                student.getGuardianPhone(),
                student.getGuardianEmail(),
                student.getEnrollmentDate(),
                student.getStatus(),
                student.getStatus() != null ? student.getStatus().getLabel() : null,
                grade != null ? ReferenceResponse.of(grade.getId(), grade.getName()) : null,
                classroom != null ? ReferenceResponse.of(classroom.getId(), classroom.getName()) : null,
                school != null ? ReferenceResponse.of(school.getId(), school.getName()) : null,
                student.getUserAccount() != null,
                student.getCreatedAt(),
                student.getUpdatedAt());
    }
}
