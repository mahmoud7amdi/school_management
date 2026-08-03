package com.smartedu.school_management_api.repository;

import com.smartedu.school_management_api.entity.Teacher;
import com.smartedu.school_management_api.entity.TeacherStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {

    @EntityGraph(attributePaths = {"school", "subjects"})
    List<Teacher> findAllByOrderByLastNameAscFirstNameAsc();

    @EntityGraph(attributePaths = {"school", "subjects"})
    List<Teacher> findBySchoolIdOrderByLastNameAscFirstNameAsc(Long schoolId);

    @EntityGraph(attributePaths = {"school", "subjects", "userAccount"})
    Optional<Teacher> findWithRelationsById(Long id);

    @EntityGraph(attributePaths = {"school", "subjects", "userAccount"})
    Optional<Teacher> findByUserAccountId(UUID userAccountId);

    boolean existsBySchoolIdAndEmployeeNumberIgnoreCase(Long schoolId, String employeeNumber);

    boolean existsBySchoolIdAndEmployeeNumberIgnoreCaseAndIdNot(Long schoolId, String employeeNumber, Long id);

    boolean existsByUserAccountId(UUID userAccountId);

    long countBySchoolId(Long schoolId);

    long countBySchoolIdAndStatus(Long schoolId, TeacherStatus status);

    /**
     * Teachers who may be given a class or section. Includes those with no subjects
     * assigned yet, so a newly-created teacher is immediately pickable.
     */
    @EntityGraph(attributePaths = {"school", "subjects"})
    @Query("""
            select t from Teacher t
            where t.status in (com.smartedu.school_management_api.entity.TeacherStatus.ACTIVE,
                               com.smartedu.school_management_api.entity.TeacherStatus.ON_LEAVE)
              and (:schoolId is null or t.school.id = :schoolId)
            order by t.lastName asc, t.firstName asc
            """)
    List<Teacher> findAssignable(@Param("schoolId") Long schoolId);
}
