package com.smartedu.school_management_api.repository;

import com.smartedu.school_management_api.entity.Student;
import com.smartedu.school_management_api.entity.StudentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    @EntityGraph(attributePaths = {"school", "grade", "classroom"})
    List<Student> findAllByOrderByLastNameAscFirstNameAsc();

    @EntityGraph(attributePaths = {"school", "grade", "classroom"})
    List<Student> findBySchoolIdOrderByLastNameAscFirstNameAsc(Long schoolId);

    @EntityGraph(attributePaths = {"school", "grade", "classroom", "userAccount"})
    Optional<Student> findWithRelationsById(Long id);

    @EntityGraph(attributePaths = {"school", "grade", "classroom"})
    List<Student> findByClassroomIdOrderByLastNameAscFirstNameAsc(Long classroomId);

    @EntityGraph(attributePaths = {"school", "grade", "classroom"})
    List<Student> findByGradeIdOrderByLastNameAscFirstNameAsc(Long gradeId);

    /** The student-portal lookup: resolves the enrolment record behind a login. */
    @EntityGraph(attributePaths = {"school", "grade", "classroom", "userAccount"})
    Optional<Student> findByUserAccountId(UUID userAccountId);

    /** Rosters for a teacher's classes, in one query rather than one per class. */
    @EntityGraph(attributePaths = {"school", "grade", "classroom"})
    List<Student> findByClassroomIdInOrderByLastNameAscFirstNameAsc(Collection<Long> classroomIds);

    /** Just the ids, for scope checks that do not need the rows. */
    @Query("select s.id from Student s where s.classroom.id in :classroomIds")
    List<Long> findIdsByClassroomIdIn(@Param("classroomIds") Collection<Long> classroomIds);

    boolean existsBySchoolIdAndAdmissionNumberIgnoreCase(Long schoolId, String admissionNumber);

    boolean existsBySchoolIdAndAdmissionNumberIgnoreCaseAndIdNot(Long schoolId, String admissionNumber, Long id);

    boolean existsByUserAccountId(UUID userAccountId);

    long countBySchoolId(Long schoolId);

    long countBySchoolIdAndStatus(Long schoolId, StudentStatus status);

    long countByStatus(StudentStatus status);

    long countByClassroomId(Long classroomId);

    long countByGradeId(Long gradeId);

    /**
     * Paged search across name, admission number and email.
     * {@code schoolId} null means "all schools" (super admin); a null/blank
     * {@code search} or {@code status} skips that predicate.
     */
    @EntityGraph(attributePaths = {"school", "grade", "classroom"})
    @Query("""
            select s from Student s
            where (:schoolId is null or s.school.id = :schoolId)
              and (:status is null or s.status = :status)
              and (:gradeId is null or s.grade.id = :gradeId)
              and (:classroomId is null or s.classroom.id = :classroomId)
              and (:search is null or :search = ''
                   or lower(s.firstName) like lower(concat('%', :search, '%'))
                   or lower(s.lastName) like lower(concat('%', :search, '%'))
                   or lower(s.admissionNumber) like lower(concat('%', :search, '%'))
                   or lower(coalesce(s.email, '')) like lower(concat('%', :search, '%')))
            """)
    Page<Student> search(@Param("schoolId") Long schoolId,
                         @Param("status") StudentStatus status,
                         @Param("gradeId") Long gradeId,
                         @Param("classroomId") Long classroomId,
                         @Param("search") String search,
                         Pageable pageable);
}
