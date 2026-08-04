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

    /**
     * Administrator accounts, optionally narrowed by school or role — the super admin's
     * view. A null parameter means "no filter" on that column.
     *
     * <p>Deliberately not "every account": appointing and maintaining administrators is
     * platform work, while the people inside a school are the school admin's to manage.
     * A role filter naming a school role therefore returns nothing rather than widening
     * the result.
     */
    @EntityGraph(attributePaths = "school")
    @Query("""
            select u from User u
            where u.role in (com.smartedu.school_management_api.entity.UserRole.SUPER_ADMIN,
                             com.smartedu.school_management_api.entity.UserRole.SCHOOL_ADMIN)
              and (:schoolId is null or u.school.id = :schoolId)
              and (:role is null or u.role = :role)
            order by u.fullName asc
            """)
    List<User> searchAdmins(@Param("schoolId") Long schoolId, @Param("role") UserRole role);

    /** As {@link #findSchoolMembers} but with the same optional role filter. */
    @EntityGraph(attributePaths = "school")
    @Query("""
            select u from User u
            where u.school.id = :schoolId
              and u.role not in (com.smartedu.school_management_api.entity.UserRole.SUPER_ADMIN,
                                 com.smartedu.school_management_api.entity.UserRole.SCHOOL_ADMIN)
              and (:role is null or u.role = :role)
            order by u.fullName asc
            """)
    List<User> searchSchoolMembers(@Param("schoolId") Long schoolId, @Param("role") UserRole role);

    @EntityGraph(attributePaths = "school")
    List<User> findBySchoolIdAndRoleOrderByFullNameAsc(Long schoolId, UserRole role);

    long countByRole(UserRole role);

    long countBySchoolId(Long schoolId);

    long countBySchoolIdAndRole(Long schoolId, UserRole role);

    /**
     * Accounts per school for one role, for the platform report. Grouped rather than
     * counted per school so the report stays a fixed number of queries.
     */
    @Query("""
            select u.school.id as schoolId, count(u) as total
            from User u
            where u.school.id is not null and u.role = :role
            group by u.school.id
            """)
    List<SchoolCountProjection> countByRoleGroupedBySchool(@Param("role") UserRole role);
}
