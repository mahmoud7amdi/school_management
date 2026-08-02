package com.smartedu.school_management_api.repository;

import com.smartedu.school_management_api.entity.Grade;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {

    @EntityGraph(attributePaths = "school")
    List<Grade> findAllByOrderByLevelOrderAscNameAsc();

    @EntityGraph(attributePaths = "school")
    List<Grade> findBySchoolIdOrderByLevelOrderAscNameAsc(Long schoolId);

    @EntityGraph(attributePaths = "school")
    Optional<Grade> findWithSchoolById(Long id);

    boolean existsBySchoolIdAndNameIgnoreCase(Long schoolId, String name);

    boolean existsBySchoolIdAndNameIgnoreCaseAndIdNot(Long schoolId, String name, Long id);

    long countBySchoolId(Long schoolId);
}
