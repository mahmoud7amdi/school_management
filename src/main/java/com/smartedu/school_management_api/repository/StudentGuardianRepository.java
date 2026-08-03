package com.smartedu.school_management_api.repository;

import com.smartedu.school_management_api.entity.StudentGuardian;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentGuardianRepository extends JpaRepository<StudentGuardian, Long> {

    @EntityGraph(attributePaths = {"parent", "student"})
    List<StudentGuardian> findByParentIdOrderByStudentLastNameAscStudentFirstNameAsc(Long parentId);

    @EntityGraph(attributePaths = {"parent", "student"})
    List<StudentGuardian> findByStudentIdOrderByPrimaryContactDescParentLastNameAsc(Long studentId);

    @EntityGraph(attributePaths = {"parent", "student"})
    Optional<StudentGuardian> findWithRelationsById(Long id);

    /** The parent-portal lookup: every child linked to a guardian's login. */
    @EntityGraph(attributePaths = {"parent", "student"})
    List<StudentGuardian> findByParentUserAccountId(UUID userAccountId);

    /**
     * Just the child ids for a guardian's login, for scope checks that do not need the
     * rows themselves.
     */
    @Query("select sg.student.id from StudentGuardian sg where sg.parent.userAccount.id = :userAccountId")
    List<Long> findStudentIdsByParentUserAccountId(@Param("userAccountId") UUID userAccountId);

    boolean existsByParentIdAndStudentId(Long parentId, Long studentId);

    long countByParentId(Long parentId);

    long countByStudentId(Long studentId);

    void deleteByParentId(Long parentId);
}
