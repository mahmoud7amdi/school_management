package com.smartedu.school_management_api.repository;

import com.smartedu.school_management_api.entity.Exam;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExamRepository extends JpaRepository<Exam, Long> {

    @EntityGraph(attributePaths = {"school", "subject", "grade", "classroom", "academicYear"})
    List<Exam> findAllByOrderByExamDateDescTitleAsc();

    @EntityGraph(attributePaths = {"school", "subject", "grade", "classroom", "academicYear"})
    List<Exam> findBySchoolIdOrderByExamDateDescTitleAsc(Long schoolId);

    @EntityGraph(attributePaths = {"school", "subject", "grade", "classroom", "academicYear"})
    Optional<Exam> findWithRelationsById(Long id);

    boolean existsByTitleAndSubjectIdAndGradeIdAndAcademicYearId(String title, Long subjectId, Long gradeId, Long yearId);

    boolean existsByTitleAndSubjectIdAndGradeIdAndAcademicYearIdAndIdNot(
            String title, Long subjectId, Long gradeId, Long yearId, Long id);

    long countBySchoolId(Long schoolId);
}
