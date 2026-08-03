package com.smartedu.school_management_api.repository;

import com.smartedu.school_management_api.entity.AbsenceNote;
import com.smartedu.school_management_api.entity.AbsenceNoteStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AbsenceNoteRepository extends JpaRepository<AbsenceNote, Long> {

    @EntityGraph(attributePaths = {"school", "student", "submittedBy", "reviewedBy"})
    List<AbsenceNote> findBySchoolIdOrderByAbsenceDateDesc(Long schoolId);

    @EntityGraph(attributePaths = {"school", "student", "submittedBy", "reviewedBy"})
    List<AbsenceNote> findAllByOrderByAbsenceDateDesc();

    @EntityGraph(attributePaths = {"school", "student", "submittedBy", "reviewedBy"})
    Optional<AbsenceNote> findWithRelationsById(Long id);

    @EntityGraph(attributePaths = {"school", "student", "submittedBy", "reviewedBy"})
    List<AbsenceNote> findByStudentIdOrderByAbsenceDateDesc(Long studentId);

    @EntityGraph(attributePaths = {"school", "student", "submittedBy", "reviewedBy"})
    List<AbsenceNote> findBySchoolIdAndStatusOrderByAbsenceDateDesc(Long schoolId, AbsenceNoteStatus status);

    /**
     * The teacher's review queue: notes for the students in the classes they teach.
     * Empty {@code classroomIds} would produce {@code in ()}, so callers must skip the
     * call rather than pass an empty collection.
     */
    @EntityGraph(attributePaths = {"school", "student", "submittedBy", "reviewedBy"})
    List<AbsenceNote> findByStudentClassroomIdInOrderByAbsenceDateDesc(Collection<Long> classroomIds);

    @EntityGraph(attributePaths = {"school", "student", "submittedBy", "reviewedBy"})
    List<AbsenceNote> findByStudentIdInOrderByAbsenceDateDesc(Collection<Long> studentIds);

    boolean existsByStudentIdAndAbsenceDate(Long studentId, LocalDate absenceDate);

    Optional<AbsenceNote> findByStudentIdAndAbsenceDate(Long studentId, LocalDate absenceDate);

    long countBySchoolIdAndStatus(Long schoolId, AbsenceNoteStatus status);

    long countByStudentIdAndStatus(Long studentId, AbsenceNoteStatus status);
}
