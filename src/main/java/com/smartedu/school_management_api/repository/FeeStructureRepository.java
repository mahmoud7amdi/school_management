package com.smartedu.school_management_api.repository;

import com.smartedu.school_management_api.entity.FeeStructure;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeeStructureRepository extends JpaRepository<FeeStructure, Long> {

    @EntityGraph(attributePaths = {"school", "grade", "academicYear"})
    List<FeeStructure> findAllByOrderByAcademicYearStartDateDescNameAsc();

    @EntityGraph(attributePaths = {"school", "grade", "academicYear"})
    List<FeeStructure> findBySchoolIdOrderByAcademicYearStartDateDescNameAsc(Long schoolId);

    @EntityGraph(attributePaths = {"school", "grade", "academicYear"})
    Optional<FeeStructure> findWithRelationsById(Long id);

    boolean existsByAcademicYearIdAndGradeIdAndNameIgnoreCase(Long academicYearId, Long gradeId, String name);

    boolean existsByAcademicYearIdAndGradeIdAndNameIgnoreCaseAndIdNot(
            Long academicYearId, Long gradeId, String name, Long id);

    long countBySchoolId(Long schoolId);

    /** Fee items that apply to one grade, for the per-student ledger. */
    @EntityGraph(attributePaths = {"school", "grade", "academicYear"})
    List<FeeStructure> findByGradeIdOrderByNameAsc(Long gradeId);
}
