package com.smartedu.school_management_api.repository;

import com.smartedu.school_management_api.entity.ExamResult;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamResultRepository extends JpaRepository<ExamResult, Long> {

    @EntityGraph(attributePaths = {"exam", "student", "school"})
    List<ExamResult> findAllByOrderByExamExamDateDescStudentLastNameAsc();

    @EntityGraph(attributePaths = {"exam", "student", "school"})
    List<ExamResult> findBySchoolIdOrderByExamExamDateDescStudentLastNameAsc(Long schoolId);

    @EntityGraph(attributePaths = {"exam", "student", "school"})
    Optional<ExamResult> findWithRelationsById(Long id);

    /** Marks for one exam, student-sorted for the entry grid. */
    @EntityGraph(attributePaths = {"exam", "student", "school"})
    List<ExamResult> findByExamIdOrderByStudentLastNameAscStudentFirstNameAsc(Long examId);

    /** One student's marks across every exam — the portal results view. */
    @EntityGraph(attributePaths = {"exam", "student", "school"})
    List<ExamResult> findByStudentIdOrderByExamExamDateDesc(Long studentId);

    long countByStudentId(Long studentId);

    boolean existsByExamIdAndStudentId(Long examId, Long studentId);

    boolean existsByExamIdAndStudentIdAndIdNot(Long examId, Long studentId, Long id);

    long countByExamId(Long examId);

    /** The class that sat an exam, via the result's student, for the entry screen. */
    @EntityGraph(attributePaths = {"exam", "student", "school"})
    List<ExamResult> findByExamIdAndStudentClassroomIdOrderByStudentLastNameAsc(Long examId, Long classroomId);

    boolean existsByExamIdAndStudentClassroomId(Long examId, Long classroomId);

    long countBySchoolId(Long schoolId);
}
