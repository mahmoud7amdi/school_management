package com.smartedu.school_management_api.mapper;

import com.smartedu.school_management_api.dto.common.ReferenceResponse;
import com.smartedu.school_management_api.dto.exam.ExamResponse;
import com.smartedu.school_management_api.dto.exam.ExamResultResponse;
import com.smartedu.school_management_api.entity.AcademicYear;
import com.smartedu.school_management_api.entity.Classroom;
import com.smartedu.school_management_api.entity.Exam;
import com.smartedu.school_management_api.entity.ExamResult;
import com.smartedu.school_management_api.entity.Grade;
import com.smartedu.school_management_api.entity.School;
import com.smartedu.school_management_api.entity.Student;
import com.smartedu.school_management_api.entity.Subject;
import org.springframework.stereotype.Component;

/** Mapping for exams and the marks recorded against them. */
@Component
public class ExamMapper {

    /** {@code resultCount} is supplied by the service so the mapper stays repository-free. */
    public ExamResponse toResponse(Exam exam, long resultCount) {
        if (exam == null) {
            return null;
        }
        Subject subject = exam.getSubject();
        Grade grade = exam.getGrade();
        Classroom classroom = exam.getClassroom();
        AcademicYear year = exam.getAcademicYear();

        return new ExamResponse(
                exam.getId(),
                exam.getTitle(),
                exam.getExamType(),
                exam.getExamType() != null ? exam.getExamType().getLabel() : null,
                exam.getExamDate(),
                exam.getStartTime(),
                exam.getDurationMinutes(),
                exam.getMaxMarks(),
                exam.getPassMarks(),
                exam.getDescription(),
                subject != null ? ReferenceResponse.of(subject.getId(), subject.getName()) : null,
                grade != null ? ReferenceResponse.of(grade.getId(), grade.getName()) : null,
                classroom != null ? ReferenceResponse.of(classroom.getId(), classroom.getName()) : null,
                year != null ? ReferenceResponse.of(year.getId(), year.getName()) : null,
                schoolRef(exam.getSchool()),
                resultCount,
                exam.getCreatedAt(),
                exam.getUpdatedAt());
    }

    public ExamResultResponse toResponse(ExamResult result) {
        if (result == null) {
            return null;
        }
        Exam exam = result.getExam();
        Student student = result.getStudent();

        return new ExamResultResponse(
                result.getId(),
                result.getMarksObtained(),
                result.getAbsent(),
                result.getPercentage(),
                result.getPassed(),
                result.getGradeLetter(),
                result.getRemarks(),
                exam != null ? ReferenceResponse.of(exam.getId(), exam.getTitle()) : null,
                exam != null ? exam.getMaxMarks() : null,
                student != null ? ReferenceResponse.of(student.getId(), student.getFullName()) : null,
                student != null ? student.getAdmissionNumber() : null,
                schoolRef(result.getSchool()),
                result.getCreatedAt(),
                result.getUpdatedAt());
    }

    private ReferenceResponse schoolRef(School school) {
        return school != null ? ReferenceResponse.of(school.getId(), school.getName()) : null;
    }
}
