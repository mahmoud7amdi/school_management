package com.smartedu.school_management_api.repository;

import com.smartedu.school_management_api.entity.Subject;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    @EntityGraph(attributePaths = {"school", "grade"})
    List<Subject> findAllByOrderByNameAsc();

    @EntityGraph(attributePaths = {"school", "grade"})
    List<Subject> findBySchoolIdOrderByNameAsc(Long schoolId);

    @EntityGraph(attributePaths = {"school", "grade"})
    List<Subject> findByGradeIdOrderByNameAsc(Long gradeId);

    @EntityGraph(attributePaths = {"school", "grade"})
    Optional<Subject> findWithRelationsById(Long id);

    boolean existsByGradeIdAndNameIgnoreCase(Long gradeId, String name);

    boolean existsByGradeIdAndNameIgnoreCaseAndIdNot(Long gradeId, String name, Long id);

    long countBySchoolId(Long schoolId);
}
