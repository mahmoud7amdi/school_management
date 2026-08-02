package com.smartedu.school_management_api.repository;

import com.smartedu.school_management_api.entity.AcademicYear;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AcademicYearRepository extends JpaRepository<AcademicYear, Long> {

    @EntityGraph(attributePaths = "school")
    List<AcademicYear> findAllByOrderByStartDateDesc();

    @EntityGraph(attributePaths = "school")
    List<AcademicYear> findBySchoolIdOrderByStartDateDesc(Long schoolId);

    @EntityGraph(attributePaths = "school")
    Optional<AcademicYear> findWithSchoolById(Long id);

    boolean existsBySchoolIdAndNameIgnoreCase(Long schoolId, String name);

    boolean existsBySchoolIdAndNameIgnoreCaseAndIdNot(Long schoolId, String name, Long id);

    /** Clears the current flag across a school so only one year can hold it. */
    @Modifying
    @Query("update AcademicYear a set a.current = false where a.school.id = :schoolId and a.id <> :keepId")
    void clearCurrentFlagExcept(@Param("schoolId") Long schoolId, @Param("keepId") Long keepId);

    long countBySchoolId(Long schoolId);
}
