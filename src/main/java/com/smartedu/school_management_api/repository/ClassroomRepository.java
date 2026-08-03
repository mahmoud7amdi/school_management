package com.smartedu.school_management_api.repository;

import com.smartedu.school_management_api.entity.Classroom;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {

    @EntityGraph(attributePaths = {"school", "grade", "academicYear", "classTeacher", "section"})
    List<Classroom> findAllByOrderByNameAsc();

    @EntityGraph(attributePaths = {"school", "grade", "academicYear", "classTeacher", "section"})
    List<Classroom> findBySchoolIdOrderByNameAsc(Long schoolId);

    @EntityGraph(attributePaths = {"school", "grade", "academicYear", "classTeacher", "section"})
    Optional<Classroom> findWithRelationsById(Long id);

    @EntityGraph(attributePaths = {"school", "grade", "academicYear", "classTeacher", "section"})
    List<Classroom> findByIdInOrderByNameAsc(Collection<Long> ids);

    /**
     * Homeroom classes for a login. Retained alongside teaching assignments because
     * {@code classTeacher} predates them, so a school that has not created assignment
     * rows yet still gives its class teachers a working portal.
     */
    @Query("select c.id from Classroom c where c.classTeacher.id = :userId")
    List<Long> findIdsByClassTeacherId(@Param("userId") UUID userId);

    boolean existsByGradeIdAndAcademicYearIdAndNameIgnoreCase(Long gradeId, Long academicYearId, String name);

    boolean existsByGradeIdAndAcademicYearIdAndNameIgnoreCaseAndIdNot(
            Long gradeId, Long academicYearId, String name, Long id);

    long countBySchoolId(Long schoolId);

    long countBySectionId(Long sectionId);
}
