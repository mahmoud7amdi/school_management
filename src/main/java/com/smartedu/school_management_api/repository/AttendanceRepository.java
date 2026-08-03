package com.smartedu.school_management_api.repository;

import com.smartedu.school_management_api.entity.Attendance;
import com.smartedu.school_management_api.entity.AttendanceStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    @EntityGraph(attributePaths = {"school", "student", "classroom", "subject", "recordedBy"})
    List<Attendance> findAllByOrderByAttendanceDateDescStudentLastNameAsc();

    @EntityGraph(attributePaths = {"school", "student", "classroom", "subject", "recordedBy"})
    List<Attendance> findBySchoolIdOrderByAttendanceDateDescStudentLastNameAsc(Long schoolId);

    @EntityGraph(attributePaths = {"school", "student", "classroom", "subject", "recordedBy"})
    Optional<Attendance> findWithRelationsById(Long id);

    @EntityGraph(attributePaths = {"school", "student", "classroom", "subject", "recordedBy"})
    List<Attendance> findByClassroomIdAndAttendanceDate(Long classroomId, LocalDate date);

    boolean existsByStudentIdAndAttendanceDate(Long studentId, LocalDate date);

    /**
     * The row the (student, date) unique constraint would collide with, whichever
     * class it was taken in.
     */
    @EntityGraph(attributePaths = {"school", "student", "classroom", "subject", "recordedBy"})
    Optional<Attendance> findByStudentIdAndAttendanceDate(Long studentId, LocalDate date);

    /** Bulk form of the above, for saving a whole register in one pass. */
    @EntityGraph(attributePaths = {"school", "student", "classroom", "subject", "recordedBy"})
    List<Attendance> findByStudentIdInAndAttendanceDate(List<Long> studentIds, LocalDate date);

    @EntityGraph(attributePaths = {"school", "student", "classroom", "subject", "recordedBy"})
    List<Attendance> findByStudentIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(Long studentId,
                                                                                   LocalDate from,
                                                                                   LocalDate to);

    /** One student's whole attendance history — the portal view, newest first. */
    @EntityGraph(attributePaths = {"school", "student", "classroom", "subject", "recordedBy"})
    List<Attendance> findByStudentIdOrderByAttendanceDateDesc(Long studentId);

    long countByStudentId(Long studentId);

    long countByStudentIdAndStatus(Long studentId, AttendanceStatus status);

    /** Today's register summary for one class: one row per student, newest mark first. */
    @EntityGraph(attributePaths = {"school", "student", "classroom", "subject", "recordedBy"})
    @Query("""
            select a from Attendance a
            where a.classroom.id = :classroomId
              and a.attendanceDate = :date
            order by a.student.lastName asc, a.student.firstName asc
            """)
    List<Attendance> findRegister(@Param("classroomId") Long classroomId, @Param("date") LocalDate date);

    long countBySchoolId(Long schoolId);

    /** Guards teacher deletion: registers keep their author for audit. */
    long countByRecordedById(Long teacherId);

    long countBySchoolIdAndAttendanceDate(Long schoolId, LocalDate date);

    long countBySchoolIdAndStatus(Long schoolId, AttendanceStatus status);

    /** Attendance-rate denominator: calendar days on which this class has a register. */
    @Query("select count(distinct a.attendanceDate) from Attendance a where a.classroom.id = :classroomId")
    long countDistinctDatesByClassroomId(@Param("classroomId") Long classroomId);

    @Query("""
            select count(a) from Attendance a
            where a.classroom.id = :classroomId and a.status = :status
            """)
    long countByClassroomIdAndStatus(@Param("classroomId") Long classroomId,
                                     @Param("status") AttendanceStatus status);
}
