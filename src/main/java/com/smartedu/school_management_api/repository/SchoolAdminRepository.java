package com.smartedu.school_management_api.repository;

import com.smartedu.school_management_api.entity.SchoolAdmin;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SchoolAdminRepository extends JpaRepository<SchoolAdmin, Long> {

    @EntityGraph(attributePaths = {"school"})
    List<SchoolAdmin> findBySchoolIdOrderByLastNameAscFirstNameAsc(Long schoolId);

    @EntityGraph(attributePaths = {"school", "userAccount"})
    Optional<SchoolAdmin> findWithRelationsById(Long id);

    @EntityGraph(attributePaths = {"school", "userAccount"})
    Optional<SchoolAdmin> findByUserAccountId(UUID userAccountId);

    boolean existsByUserAccountId(UUID userAccountId);

    boolean existsBySchoolIdAndEmailIgnoreCase(Long schoolId, String email);

    long countBySchoolId(Long schoolId);
}
