package com.smartedu.school_management_api.repository;

import com.smartedu.school_management_api.entity.Parent;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParentRepository extends JpaRepository<Parent, Long> {

    @EntityGraph(attributePaths = {"school"})
    List<Parent> findAllByOrderByLastNameAscFirstNameAsc();

    @EntityGraph(attributePaths = {"school"})
    List<Parent> findBySchoolIdOrderByLastNameAscFirstNameAsc(Long schoolId);

    @EntityGraph(attributePaths = {"school", "userAccount"})
    Optional<Parent> findWithRelationsById(Long id);

    /** The parent-portal lookup: resolves the guardian record behind a login. */
    @EntityGraph(attributePaths = {"school", "userAccount"})
    Optional<Parent> findByUserAccountId(UUID userAccountId);

    boolean existsByUserAccountId(UUID userAccountId);

    boolean existsBySchoolIdAndEmailIgnoreCase(Long schoolId, String email);

    boolean existsBySchoolIdAndEmailIgnoreCaseAndIdNot(Long schoolId, String email, Long id);

    long countBySchoolId(Long schoolId);
}
