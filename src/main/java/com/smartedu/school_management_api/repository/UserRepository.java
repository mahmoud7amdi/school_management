package com.smartedu.school_management_api.repository;

import com.smartedu.school_management_api.entity.User;
import com.smartedu.school_management_api.entity.UserRole;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsernameIgnoreCase(String username);

    @EntityGraph(attributePaths = "school")
    Optional<User> findWithSchoolByUsernameIgnoreCase(String username);

    @EntityGraph(attributePaths = "school")
    Optional<User> findWithSchoolById(UUID id);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    /** Uniqueness check that ignores the row being updated. */
    boolean existsByUsernameIgnoreCaseAndIdNot(String username, UUID id);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);

    @EntityGraph(attributePaths = "school")
    List<User> findByRoleOrderByFullNameAsc(UserRole role);

    /** Staff visible to a school admin: everyone in the school bar the two admin tiers. */
    @EntityGraph(attributePaths = "school")
    @Query("""
            select u from User u
            where u.school.id = :schoolId
              and u.role not in (com.smartedu.school_management_api.entity.UserRole.SUPER_ADMIN,
                                 com.smartedu.school_management_api.entity.UserRole.SCHOOL_ADMIN)
            order by u.fullName asc
            """)
    List<User> findSchoolMembers(@Param("schoolId") Long schoolId);

    @EntityGraph(attributePaths = "school")
    List<User> findBySchoolIdAndRoleOrderByFullNameAsc(Long schoolId, UserRole role);

    long countByRole(UserRole role);

    long countBySchoolId(Long schoolId);

    long countBySchoolIdAndRole(Long schoolId, UserRole role);
}
