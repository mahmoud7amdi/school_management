package com.smartedu.school_management_api.repository;

import com.smartedu.school_management_api.entity.Section;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {

    @EntityGraph(attributePaths = {"school", "grade", "sectionHead"})
    List<Section> findAllByOrderByGradeLevelOrderAscNameAsc();

    @EntityGraph(attributePaths = {"school", "grade", "sectionHead"})
    List<Section> findBySchoolIdOrderByGradeLevelOrderAscNameAsc(Long schoolId);

    @EntityGraph(attributePaths = {"school", "grade", "sectionHead"})
    Optional<Section> findWithRelationsById(Long id);

    boolean existsByGradeIdAndNameIgnoreCase(Long gradeId, String name);

    boolean existsByGradeIdAndNameIgnoreCaseAndIdNot(Long gradeId, String name, Long id);

    long countBySchoolId(Long schoolId);

    /** Guards teacher deletion: a section head must be reassigned first. */
    long countBySectionHeadId(Long teacherId);
}
