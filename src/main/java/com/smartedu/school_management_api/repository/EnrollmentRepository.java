package com.smartedu.school_management_api.repository;

import com.smartedu.school_management_api.entity.Enrollment;
import com.smartedu.school_management_api.entity.EnrollmentStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    @EntityGraph(attributePaths = {"school", "student", "academicYear", "grade", "classroom"})
    List<Enrollment> findAllByOrderByAcademicYearStartDateDescStudentLastNameAsc();

    @EntityGraph(attributePaths = {"school", "student", "academicYear", "grade", "classroom"})
    List<Enrollment> findBySchoolIdOrderByAcademicYearStartDateDescStudentLastNameAsc(Long schoolId);

    @EntityGraph(attributePaths = {"school", "student", "academicYear", "grade", "classroom"})
    Optional<Enrollment> findWithRelationsById(Long id);

    boolean existsByStudentIdAndAcademicYearId(Long studentId, Long academicYearId);

    boolean existsByStudentIdAndAcademicYearIdAndIdNot(Long studentId, Long academicYearId, Long id);

    @EntityGraph(attributePaths = {"school", "student", "academicYear", "grade", "classroom"})
    List<Enrollment> findByStudentIdOrderByAcademicYearStartDateDesc(Long studentId);

    @EntityGraph(attributePaths = {"school", "student", "academicYear", "grade", "classroom"})
    List<Enrollment> findByAcademicYearIdOrderByStudentLastNameAsc(Long academicYearId);

    /** The occupancy-relevant rows: open statuses for a classroom this year. */
    @EntityGraph(attributePaths = {"school", "student", "academicYear", "grade", "classroom"})
    List<Enrollment> findByClassroomIdAndStatusIn(Long classroomId, List<EnrollmentStatus> statuses);
}
