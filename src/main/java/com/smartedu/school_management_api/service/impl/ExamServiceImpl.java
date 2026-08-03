package com.smartedu.school_management_api.service.impl;

import com.smartedu.school_management_api.dto.exam.ExamRequest;
import com.smartedu.school_management_api.dto.exam.ExamResponse;
import com.smartedu.school_management_api.dto.exam.ExamResultBulkRequest;
import com.smartedu.school_management_api.dto.exam.ExamResultResponse;
import com.smartedu.school_management_api.entity.AcademicYear;
import com.smartedu.school_management_api.entity.Classroom;
import com.smartedu.school_management_api.entity.Exam;
import com.smartedu.school_management_api.entity.ExamResult;
import com.smartedu.school_management_api.entity.Grade;
import com.smartedu.school_management_api.entity.Student;
import com.smartedu.school_management_api.entity.Subject;
import com.smartedu.school_management_api.exception.BadRequestException;
import com.smartedu.school_management_api.exception.DuplicateResourceException;
import com.smartedu.school_management_api.exception.NotFoundException;
import com.smartedu.school_management_api.mapper.ExamMapper;
import com.smartedu.school_management_api.repository.AcademicYearRepository;
import com.smartedu.school_management_api.repository.ClassroomRepository;
import com.smartedu.school_management_api.repository.ExamRepository;
import com.smartedu.school_management_api.repository.ExamResultRepository;
import com.smartedu.school_management_api.repository.StudentRepository;
import com.smartedu.school_management_api.repository.SubjectRepository;
import com.smartedu.school_management_api.service.ExamService;
import com.smartedu.school_management_api.service.PortalAccessService;
import com.smartedu.school_management_api.service.SchoolAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamService {

    private final ExamRepository examRepository;
    private final ExamResultRepository examResultRepository;
    private final SubjectRepository subjectRepository;
    private final ClassroomRepository classroomRepository;
    private final AcademicYearRepository academicYearRepository;
    private final StudentRepository studentRepository;
    private final SchoolAccessService access;
    private final PortalAccessService portalAccess;
    private final ExamMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<ExamResponse> getAllExams() {
        Long schoolId = access.schoolScopeForCurrentUser();
        List<Exam> exams = schoolId == null
                ? examRepository.findAllByOrderByExamDateDescTitleAsc()
                : examRepository.findBySchoolIdOrderByExamDateDescTitleAsc(schoolId);

        return exams.stream()
                .map(exam -> mapper.toResponse(exam, examResultRepository.countByExamId(exam.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ExamResponse getExamById(Long id) {
        // Readable by a teacher who marks the paper: the marks grid loads the exam for its
        // maximum and pass marks before it can render, let alone save.
        Exam exam = loadForMarking(id);
        return mapper.toResponse(exam, examResultRepository.countByExamId(id));
    }

    @Override
    @Transactional
    public ExamResponse createExam(ExamRequest request) {
        // The subject pins the grade (and therefore the school), so every other
        // reference is validated against that anchor.
        Subject subject = loadAccessibleSubject(request.subjectId());
        Long schoolId = subject.getSchool().getId();
        Grade grade = subject.getGrade();

        AcademicYear year = loadAccessibleYear(request.academicYearId(), schoolId);
        Classroom classroom = resolveClassroom(request.classroomId(), grade, year, schoolId);
        validateMarks(request);
        String title = request.title().trim();

        if (examRepository.existsByTitleAndSubjectIdAndGradeIdAndAcademicYearId(
                title, subject.getId(), grade.getId(), year.getId())) {
            throw new DuplicateResourceException(
                    "An exam titled '" + title + "' already exists for this subject, grade and academic year");
        }

        Exam exam = Exam.builder()
                .title(title)
                .examType(request.examType())
                .examDate(request.examDate())
                .startTime(request.startTime())
                .durationMinutes(request.durationMinutes())
                .maxMarks(request.maxMarks())
                .passMarks(request.passMarks())
                .description(trimToNull(request.description()))
                .subject(subject)
                .grade(grade)
                .classroom(classroom)
                .academicYear(year)
                .school(subject.getSchool())
                .build();

        return mapper.toResponse(examRepository.save(exam), 0L);
    }

    @Override
    @Transactional
    public ExamResponse updateExam(Long id, ExamRequest request) {
        Exam exam = loadAccessible(id);
        Subject subject = loadAccessibleSubject(request.subjectId());
        Long schoolId = subject.getSchool().getId();
        Grade grade = subject.getGrade();

        AcademicYear year = loadAccessibleYear(request.academicYearId(), schoolId);
        Classroom classroom = resolveClassroom(request.classroomId(), grade, year, schoolId);
        validateMarks(request);
        String title = request.title().trim();

        if (examRepository.existsByTitleAndSubjectIdAndGradeIdAndAcademicYearIdAndIdNot(
                title, subject.getId(), grade.getId(), year.getId(), id)) {
            throw new DuplicateResourceException(
                    "An exam titled '" + title + "' already exists for this subject, grade and academic year");
        }

        exam.setTitle(title);
        exam.setExamType(request.examType());
        exam.setExamDate(request.examDate());
        exam.setStartTime(request.startTime());
        exam.setDurationMinutes(request.durationMinutes());
        exam.setMaxMarks(request.maxMarks());
        exam.setPassMarks(request.passMarks());
        exam.setDescription(trimToNull(request.description()));
        exam.setSubject(subject);
        exam.setGrade(grade);
        exam.setClassroom(classroom);
        exam.setAcademicYear(year);
        exam.setSchool(subject.getSchool());

        return mapper.toResponse(examRepository.save(exam), examResultRepository.countByExamId(id));
    }

    @Override
    @Transactional
    public void deleteExam(Long id) {
        examRepository.delete(loadAccessible(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExamResultResponse> getResultsForExam(Long examId) {
        Exam exam = loadForMarking(examId);
        return examResultRepository.findByExamIdOrderByStudentLastNameAscStudentFirstNameAsc(exam.getId())
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional
    public List<ExamResultResponse> saveResults(ExamResultBulkRequest request) {
        Exam exam = loadForMarking(request.examId());
        Long schoolId = exam.getSchool().getId();

        // The unique constraint is (exam, student); fetch whatever already exists for
        // this exam so re-saving the grid updates rows instead of colliding.
        Map<Long, ExamResult> existingByStudent = new LinkedHashMap<>();
        examResultRepository.findByExamIdOrderByStudentLastNameAscStudentFirstNameAsc(exam.getId())
                .forEach(result -> existingByStudent.put(result.getStudent().getId(), result));

        List<ExamResult> saved = new ArrayList<>();
        for (ExamResultBulkRequest.Entry entry : request.entries()) {
            // Not loadAccessibleStudent: that gate rejects teachers. The school check
            // below keeps the write inside the tenant, and loadForMarking above has
            // already confirmed the caller may mark this paper.
            Student student = studentRepository.findWithRelationsById(entry.studentId())
                    .orElseThrow(() -> NotFoundException.of("Student", entry.studentId()));
            if (!student.getSchool().getId().equals(schoolId)) {
                throw new NotFoundException("Student not found in this school: " + entry.studentId());
            }
            validateMarksAgainstExam(entry.marksObtained(), exam);

            ExamResult result = existingByStudent.get(student.getId());
            if (result == null) {
                result = ExamResult.builder()
                        .exam(exam)
                        .student(student)
                        .school(exam.getSchool())
                        .build();
            }
            result.setMarksObtained(entry.marksObtained());
            result.setAbsent(Boolean.TRUE.equals(entry.absent()));
            result.setRemarks(trimToNull(entry.remarks()));
            saved.add(examResultRepository.save(result));
        }
        return saved.stream().map(mapper::toResponse).toList();
    }

    // ------------------------------------------------------------------ helpers

    private Exam loadAccessible(Long id) {
        Exam exam = examRepository.findWithRelationsById(id)
                .orElseThrow(() -> NotFoundException.of("Exam", id));
        access.requireSchoolAccess(exam.getSchool().getId());
        return exam;
    }

    /**
     * An exam the caller may read and mark.
     *
     * <p>Separate from {@link #loadAccessible}, which stays admin-only and still guards
     * scheduling an exam (create, update, delete). Marking is the part a teacher shares,
     * so it authorises against the paper's class — or its grade, when the paper is
     * grade-wide and has no class.
     */
    private Exam loadForMarking(Long id) {
        Exam exam = examRepository.findWithRelationsById(id)
                .orElseThrow(() -> NotFoundException.of("Exam", id));

        portalAccess.requireExamRecordAccess(
                exam.getClassroom() != null ? exam.getClassroom().getId() : null,
                exam.getGrade() != null ? exam.getGrade().getId() : null,
                exam.getSchool().getId());
        return exam;
    }

    private Subject loadAccessibleSubject(Long subjectId) {
        Subject subject = subjectRepository.findWithRelationsById(subjectId)
                .orElseThrow(() -> NotFoundException.of("Subject", subjectId));
        access.requireSchoolAccess(subject.getSchool().getId());
        return subject;
    }

    private AcademicYear loadAccessibleYear(Long yearId, Long schoolId) {
        AcademicYear year = academicYearRepository.findWithSchoolById(yearId)
                .orElseThrow(() -> NotFoundException.of("Academic year", yearId));
        if (!year.getSchool().getId().equals(schoolId)) {
            throw new BadRequestException("The academic year belongs to a different school");
        }
        return year;
    }

    private Classroom resolveClassroom(Long classroomId, Grade grade, AcademicYear year, Long schoolId) {
        if (classroomId == null) {
            return null;
        }
        Classroom classroom = classroomRepository.findWithRelationsById(classroomId)
                .orElseThrow(() -> NotFoundException.of("Classroom", classroomId));

        if (!classroom.getSchool().getId().equals(schoolId)) {
            throw new BadRequestException("The classroom belongs to a different school");
        }
        if (!classroom.getGrade().getId().equals(grade.getId())) {
            throw new BadRequestException("The classroom does not match the subject's grade");
        }
        if (!classroom.getAcademicYear().getId().equals(year.getId())) {
            throw new BadRequestException("The classroom belongs to a different academic year");
        }
        return classroom;
    }

    private void validateMarks(ExamRequest request) {
        if (request.passMarks().compareTo(request.maxMarks()) > 0) {
            throw new BadRequestException("Pass marks cannot exceed the maximum marks");
        }
    }

    private void validateMarksAgainstExam(BigDecimal marks, Exam exam) {
        if (marks == null) {
            return;
        }
        if (marks.compareTo(exam.getMaxMarks()) > 0) {
            throw new BadRequestException("Marks cannot exceed the exam maximum of " + exam.getMaxMarks());
        }
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
