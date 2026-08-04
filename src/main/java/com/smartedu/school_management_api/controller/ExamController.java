package com.smartedu.school_management_api.controller;

import com.smartedu.school_management_api.dto.ApiResponse;
import com.smartedu.school_management_api.dto.exam.ExamRequest;
import com.smartedu.school_management_api.dto.exam.ExamResponse;
import com.smartedu.school_management_api.dto.exam.ExamResultBulkRequest;
import com.smartedu.school_management_api.dto.exam.ExamResultResponse;
import com.smartedu.school_management_api.exception.BadRequestException;
import com.smartedu.school_management_api.service.ExamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/exams")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SCHOOL_ADMIN')")
public class ExamController {

    private final ExamService examService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ExamResponse>>> getAllExams() {
        return ResponseEntity.ok(ApiResponse.ok(examService.getAllExams(), "Exams loaded"));
    }

    /**
     * Open to teachers: the marks grid loads the paper for its maximum and pass marks
     * before it can render. The service authorises against the paper's class or grade.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SCHOOL_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<ExamResponse>> getExamById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(examService.getExamById(id), "Exam loaded"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ExamResponse>> createExam(@Valid @RequestBody ExamRequest request) {
        ExamResponse created = examService.createExam(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(created, "Exam added successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExamResponse>> updateExam(@PathVariable Long id,
                                                               @Valid @RequestBody ExamRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(examService.updateExam(id, request), "Exam updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteExam(@PathVariable Long id) {
        examService.deleteExam(id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", "Exam deleted successfully"));
    }

    // --- Results ----------------------------------------------------------

    @GetMapping("/{id}/results")
    @PreAuthorize("hasAnyAuthority('SCHOOL_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<List<ExamResultResponse>>> getResults(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(examService.getResultsForExam(id), "Results loaded"));
    }

    /**
     * Saves a whole marks grid in one call, upserting on (exam, student).
     *
     * <p>The path is authoritative: a body naming a different exam is rejected rather
     * than silently writing marks against the wrong paper.
     */
    @PostMapping("/{id}/results")
    @PreAuthorize("hasAnyAuthority('SCHOOL_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<List<ExamResultResponse>>> saveResults(
            @PathVariable Long id, @Valid @RequestBody ExamResultBulkRequest request) {
        if (request.examId() != null && !request.examId().equals(id)) {
            throw new BadRequestException("The exam in the request body does not match the exam being updated");
        }
        return ResponseEntity.ok(ApiResponse.ok(examService.saveResults(request), "Results saved successfully"));
    }
}
