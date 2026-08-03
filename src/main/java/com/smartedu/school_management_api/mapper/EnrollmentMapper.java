package com.smartedu.school_management_api.mapper;

import com.smartedu.school_management_api.dto.attendance.AttendanceResponse;
import com.smartedu.school_management_api.dto.common.ReferenceResponse;
import com.smartedu.school_management_api.dto.enrollment.EnrollmentResponse;
import com.smartedu.school_management_api.entity.AcademicYear;
import com.smartedu.school_management_api.entity.Attendance;
import com.smartedu.school_management_api.entity.Classroom;
import com.smartedu.school_management_api.entity.Enrollment;
import com.smartedu.school_management_api.entity.Grade;
import com.smartedu.school_management_api.entity.School;
import com.smartedu.school_management_api.entity.Student;
import com.smartedu.school_management_api.entity.Subject;
import com.smartedu.school_management_api.entity.Teacher;
import org.springframework.stereotype.Component;

/** Mapping for the per-student records: yearly enrolments and daily attendance. */
@Component
public class EnrollmentMapper {

    public EnrollmentResponse toResponse(Enrollment enrollment) {
        if (enrollment == null) {
            return null;
        }
        Student student = enrollment.getStudent();
        AcademicYear year = enrollment.getAcademicYear();
        Grade grade = enrollment.getGrade();
        Classroom classroom = enrollment.getClassroom();

        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getRollNumber(),
                enrollment.getEnrollmentDate(),
                enrollment.getCompletionDate(),
                enrollment.getStatus(),
                enrollment.getStatus() != null ? enrollment.getStatus().getLabel() : null,
                enrollment.getRemarks(),
                student != null ? ReferenceResponse.of(student.getId(), student.getFullName()) : null,
                student != null ? student.getAdmissionNumber() : null,
                year != null ? ReferenceResponse.of(year.getId(), year.getName()) : null,
                grade != null ? ReferenceResponse.of(grade.getId(), grade.getName()) : null,
                classroom != null ? ReferenceResponse.of(classroom.getId(), classroom.getName()) : null,
                schoolRef(enrollment.getSchool()),
                enrollment.getCreatedAt(),
                enrollment.getUpdatedAt());
    }

    public AttendanceResponse toResponse(Attendance attendance) {
        if (attendance == null) {
            return null;
        }
        Student student = attendance.getStudent();
        Classroom classroom = attendance.getClassroom();
        Subject subject = attendance.getSubject();
        Teacher recordedBy = attendance.getRecordedBy();

        return new AttendanceResponse(
                attendance.getId(),
                attendance.getAttendanceDate(),
                attendance.getStatus(),
                attendance.getStatus() != null ? attendance.getStatus().getLabel() : null,
                attendance.getRemarks(),
                student != null ? ReferenceResponse.of(student.getId(), student.getFullName()) : null,
                classroom != null ? ReferenceResponse.of(classroom.getId(), classroom.getName()) : null,
                subject != null ? ReferenceResponse.of(subject.getId(), subject.getName()) : null,
                recordedBy != null ? ReferenceResponse.of(recordedBy.getId(), recordedBy.getFullName()) : null,
                schoolRef(attendance.getSchool()),
                attendance.getCreatedAt(),
                attendance.getUpdatedAt());
    }

    private ReferenceResponse schoolRef(School school) {
        return school != null ? ReferenceResponse.of(school.getId(), school.getName()) : null;
    }
}
