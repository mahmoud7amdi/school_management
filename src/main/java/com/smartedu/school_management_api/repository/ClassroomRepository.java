package com.smartedu.school_management_api.repository;

import com.smartedu.school_management_api.entity.Classroom;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassroomRepository extends JpaRepository<Classroom, Long> {

    @EntityGraph(attributePaths = {"school", "grade", "academicYear", "classTeacher"})
    List<Classroom> findAllByOrderByNameAsc();

    @EntityGraph(attributePaths = {"school", "grade", "academicYear", "classTeacher"})
    List<Classroom> findBySchoolIdOrderByNameAsc(Long schoolId);

    @EntityGraph(attributePaths = {"school", "grade", "academicYear", "classTeacher"})
    Optional<Classroom> findWithRelationsById(Long id);

    boolean existsByGradeIdAndAcademicYearIdAndNameIgnoreCase(Long gradeId, Long academicYearId, String name);

    boolean existsByGradeIdAndAcademicYearIdAndNameIgnoreCaseAndIdNot(
            Long gradeId, Long academicYearId, String name, Long id);

    long countBySchoolId(Long schoolId);
}
