package com.smartedu.school_management_api.service;

import com.smartedu.school_management_api.dto.exam.ExamRequest;
import com.smartedu.school_management_api.dto.exam.ExamResponse;
import com.smartedu.school_management_api.dto.exam.ExamResultBulkRequest;
import com.smartedu.school_management_api.dto.exam.ExamResultResponse;

import java.util.List;

public interface ExamService {

    List<ExamResponse> getAllExams();

    ExamResponse getExamById(Long id);

    ExamResponse createExam(ExamRequest request);

    ExamResponse updateExam(Long id, ExamRequest request);

    void deleteExam(Long id);

    /** Marks recorded for one exam, student-sorted for the entry grid. */
    List<ExamResultResponse> getResultsForExam(Long examId);

    /** Saves a whole marks grid, upserting on (exam, student). Returns the saved rows. */
    List<ExamResultResponse> saveResults(ExamResultBulkRequest request);
}
