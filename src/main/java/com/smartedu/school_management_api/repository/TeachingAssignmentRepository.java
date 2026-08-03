package com.smartedu.school_management_api.repository;

import com.smartedu.school_management_api.entity.TeachingAssignment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeachingAssignmentRepository extends JpaRepository<TeachingAssignment, Long> {

    @EntityGraph(attributePaths = {"school", "teacher", "classroom", "subject", "academicYear"})
    List<TeachingAssignment> findBySchoolIdOrderByClassroomNameAsc(Long schoolId);

    @EntityGraph(attributePaths = {"school", "teacher", "classroom", "subject", "academicYear"})
    List<TeachingAssignment> findAllByOrderByClassroomNameAsc();

    @EntityGraph(attributePaths = {"school", "teacher", "classroom", "subject", "academicYear"})
    Optional<TeachingAssignment> findWithRelationsById(Long id);

    @EntityGraph(attributePaths = {"school", "teacher", "classroom", "subject", "academicYear"})
    List<TeachingAssignment> findByTeacherIdOrderByClassroomNameAsc(Long teacherId);

    @EntityGraph(attributePaths = {"school", "teacher", "classroom", "subject", "academicYear"})
    List<TeachingAssignment> findByClassroomIdOrderBySubjectNameAsc(Long classroomId);

    /** The teacher-portal lookup: every assignment behind a staff login. */
    @EntityGraph(attributePaths = {"school", "teacher", "classroom", "subject", "academicYear"})
    List<TeachingAssignment> findByTeacherUserAccountId(UUID userAccountId);

    /**
     * The classroom ids a staff login is assigned to.
     *
     * <p>This is the read side of a teacher's scope, so it is kept as a projection: the
     * caller only ever needs the ids, and loading whole rows to discard them would be
     * wasteful on every request.
     */
    @Query("""
            select distinct ta.classroom.id from TeachingAssignment ta
            where ta.teacher.userAccount.id = :userAccountId
            """)
    List<Long> findClassroomIdsByTeacherUserAccountId(@Param("userAccountId") UUID userAccountId);

    /**
     * Duplicate guard. Written as JPQL rather than derived because {@code subjectId} is
     * nullable, and a derived query would emit {@code subject_id = null}, which never
     * matches in SQL — so a second whole-class assignment would slip through.
     */
    @Query("""
            select count(ta) > 0 from TeachingAssignment ta
            where ta.teacher.id = :teacherId
              and ta.classroom.id = :classroomId
              and ta.academicYear.id = :academicYearId
              and ((:subjectId is null and ta.subject is null) or ta.subject.id = :subjectId)
            """)
    boolean existsAssignment(@Param("teacherId") Long teacherId,
                             @Param("classroomId") Long classroomId,
                             @Param("subjectId") Long subjectId,
                             @Param("academicYearId") Long academicYearId);

    /** As {@link #existsAssignment} but ignoring the row being updated. */
    @Query("""
            select count(ta) > 0 from TeachingAssignment ta
            where ta.teacher.id = :teacherId
              and ta.classroom.id = :classroomId
              and ta.academicYear.id = :academicYearId
              and ((:subjectId is null and ta.subject is null) or ta.subject.id = :subjectId)
              and ta.id <> :id
            """)
    boolean existsAssignmentExcluding(@Param("teacherId") Long teacherId,
                                      @Param("classroomId") Long classroomId,
                                      @Param("subjectId") Long subjectId,
                                      @Param("academicYearId") Long academicYearId,
                                      @Param("id") Long id);

    long countByTeacherId(Long teacherId);

    long countByClassroomId(Long classroomId);

    long countBySchoolId(Long schoolId);
}
