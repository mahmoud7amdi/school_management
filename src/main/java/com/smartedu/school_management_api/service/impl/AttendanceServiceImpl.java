package com.smartedu.school_management_api.service.impl;

import com.smartedu.school_management_api.dto.attendance.AttendanceBulkRequest;
import com.smartedu.school_management_api.dto.attendance.AttendanceRequest;
import com.smartedu.school_management_api.dto.attendance.AttendanceResponse;
import com.smartedu.school_management_api.entity.Attendance;
import com.smartedu.school_management_api.entity.Classroom;
import com.smartedu.school_management_api.entity.Student;
import com.smartedu.school_management_api.entity.Subject;
import com.smartedu.school_management_api.entity.Teacher;
import com.smartedu.school_management_api.exception.DuplicateResourceException;
import com.smartedu.school_management_api.exception.NotFoundException;
import com.smartedu.school_management_api.mapper.EnrollmentMapper;
import com.smartedu.school_management_api.repository.AttendanceRepository;
import com.smartedu.school_management_api.repository.ClassroomRepository;
import com.smartedu.school_management_api.repository.StudentRepository;
import com.smartedu.school_management_api.repository.SubjectRepository;
import com.smartedu.school_management_api.repository.TeacherRepository;
import com.smartedu.school_management_api.service.AttendanceService;
import com.smartedu.school_management_api.service.PortalAccessService;
import com.smartedu.school_management_api.service.SchoolAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final ClassroomRepository classroomRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;
    private final SchoolAccessService access;
    private final PortalAccessService portalAccess;
    private final EnrollmentMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAllAttendance() {
        Long schoolId = access.schoolScopeForCurrentUser();
        List<Attendance> records = schoolId == null
                ? attendanceRepository.findAllByOrderByAttendanceDateDescStudentLastNameAsc()
                : attendanceRepository.findBySchoolIdOrderByAttendanceDateDescStudentLastNameAsc(schoolId);
        return records.stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceResponse getAttendanceById(Long id) {
        return mapper.toResponse(loadAccessible(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getRegister(Long classroomId, LocalDate date) {
        // Widened alongside saveRegister: the grid has to read the existing marks before
        // it can save them, so gating the read to admins would leave the write unusable.
        Classroom classroom = classroomRepository.findWithRelationsById(classroomId)
                .orElseThrow(() -> NotFoundException.of("Classroom", classroomId));
        portalAccess.requireClassroomWriteAccess(classroom.getId(), classroom.getSchool().getId());

        return attendanceRepository.findRegister(classroom.getId(), date)
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional
    public AttendanceResponse createAttendance(AttendanceRequest request) {
        Student student = loadAccessibleStudent(request.studentId());
        Long schoolId = student.getSchool().getId();

        Classroom classroom = loadAccessibleClassroom(request.classroomId());
        assertSameSchool(classroom, schoolId);

        Subject subject = resolveSubject(request.subjectId(), schoolId);
        Teacher recordedBy = resolveRecordedBy(request.recordedById(), schoolId);

        if (attendanceRepository.existsByStudentIdAndAttendanceDate(student.getId(), request.attendanceDate())) {
            throw new DuplicateResourceException(
                    "Attendance for " + student.getFullName() + " on " + request.attendanceDate()
                            + " already exists. Use the register endpoint to update a whole day.");
        }

        Attendance attendance = Attendance.builder()
                .attendanceDate(request.attendanceDate())
                .status(request.status())
                .remarks(trimToNull(request.remarks()))
                .student(student)
                .classroom(classroom)
                .subject(subject)
                .recordedBy(recordedBy)
                .school(student.getSchool())
                .build();

        return mapper.toResponse(attendanceRepository.save(attendance));
    }

    @Override
    @Transactional
    public AttendanceResponse updateAttendance(Long id, AttendanceRequest request) {
        Attendance attendance = loadAccessible(id);
        Student student = loadAccessibleStudent(request.studentId());
        Long schoolId = student.getSchool().getId();

        Classroom classroom = loadAccessibleClassroom(request.classroomId());
        assertSameSchool(classroom, schoolId);

        Subject subject = resolveSubject(request.subjectId(), schoolId);
        Teacher recordedBy = resolveRecordedBy(request.recordedById(), schoolId);

        attendance.setAttendanceDate(request.attendanceDate());
        attendance.setStatus(request.status());
        attendance.setRemarks(trimToNull(request.remarks()));
        attendance.setStudent(student);
        attendance.setClassroom(classroom);
        attendance.setSubject(subject);
        attendance.setRecordedBy(recordedBy);
        attendance.setSchool(student.getSchool());

        return mapper.toResponse(attendanceRepository.save(attendance));
    }

    @Override
    @Transactional
    public List<AttendanceResponse> saveRegister(AttendanceBulkRequest request) {
        // The register is the one write a teacher shares with an admin, so it resolves
        // the classroom first and authorises against it: an admin by the tenant rule, a
        // teacher only for a class they are assigned to.
        Classroom classroom = classroomRepository.findWithRelationsById(request.classroomId())
                .orElseThrow(() -> NotFoundException.of("Classroom", request.classroomId()));
        Long schoolId = classroom.getSchool().getId();
        portalAccess.requireClassroomWriteAccess(classroom.getId(), schoolId);

        Subject subject = resolveSubject(request.subjectId(), schoolId);
        Teacher recordedBy = resolveRegisterAuthor(request.recordedById(), schoolId);

        // The unique constraint is (student, date) across the whole school, so an
        // existing mark may live in a different class than the one on this register.
        // Load those rows up front and re-point them at this class rather than
        // letting the save trip the constraint.
        Map<Long, Attendance> existingByStudent = new LinkedHashMap<>();
        attendanceRepository.findByStudentIdInAndAttendanceDate(request.entries().stream()
                        .map(AttendanceBulkRequest.Entry::studentId).toList(), request.attendanceDate())
                .forEach(record -> existingByStudent.put(record.getStudent().getId(), record));

        List<Attendance> saved = new ArrayList<>();
        for (AttendanceBulkRequest.Entry entry : request.entries()) {
            // Not loadAccessibleStudent: that gate rejects teachers. The school check
            // below is what keeps the write inside the tenant, and the classroom
            // authorisation above is what keeps it inside the teacher's own classes.
            Student student = studentRepository.findWithRelationsById(entry.studentId())
                    .orElseThrow(() -> NotFoundException.of("Student", entry.studentId()));
            if (!student.getSchool().getId().equals(schoolId)) {
                throw new NotFoundException("Student not found in this school: " + entry.studentId());
            }

            Attendance record = existingByStudent.get(student.getId());
            if (record == null) {
                record = Attendance.builder()
                        .attendanceDate(request.attendanceDate())
                        .student(student)
                        .classroom(classroom)
                        .school(classroom.getSchool())
                        .build();
            }
            record.setStatus(entry.status());
            record.setRemarks(trimToNull(entry.remarks()));
            record.setClassroom(classroom);
            record.setSubject(subject);
            record.setRecordedBy(recordedBy);
            saved.add(attendanceRepository.save(record));
        }
        return saved.stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional
    public void deleteAttendance(Long id) {
        attendanceRepository.delete(loadAccessible(id));
    }

    // ------------------------------------------------------------------ helpers

    private Attendance loadAccessible(Long id) {
        Attendance attendance = attendanceRepository.findWithRelationsById(id)
                .orElseThrow(() -> NotFoundException.of("Attendance", id));
        access.requireSchoolAccess(attendance.getSchool().getId());
        return attendance;
    }

    private Student loadAccessibleStudent(Long studentId) {
        Student student = studentRepository.findWithRelationsById(studentId)
                .orElseThrow(() -> NotFoundException.of("Student", studentId));
        access.requireSchoolAccess(student.getSchool().getId());
        return student;
    }

    private Classroom loadAccessibleClassroom(Long classroomId) {
        Classroom classroom = classroomRepository.findWithRelationsById(classroomId)
                .orElseThrow(() -> NotFoundException.of("Classroom", classroomId));
        access.requireSchoolAccess(classroom.getSchool().getId());
        return classroom;
    }

    private void assertSameSchool(Classroom classroom, Long schoolId) {
        if (!classroom.getSchool().getId().equals(schoolId)) {
            throw new NotFoundException("Classroom not found in this school: " + classroom.getId());
        }
    }

    private Subject resolveSubject(Long subjectId, Long schoolId) {
        if (subjectId == null) {
            return null;
        }
        Subject subject = subjectRepository.findWithRelationsById(subjectId)
                .orElseThrow(() -> NotFoundException.of("Subject", subjectId));
        if (!subject.getSchool().getId().equals(schoolId)) {
            throw new NotFoundException("Subject not found in this school: " + subjectId);
        }
        return subject;
    }

    private Teacher resolveRecordedBy(Long teacherId, Long schoolId) {
        if (teacherId == null) {
            return null;
        }
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> NotFoundException.of("Teacher", teacherId));
        if (!teacher.getSchool().getId().equals(schoolId)) {
            throw new NotFoundException("Teacher not found in this school: " + teacherId);
        }
        return teacher;
    }

    /**
     * Who a saved register is attributed to.
     *
     * <p>A teacher always signs their own register: the client-supplied
     * {@code recordedById} is ignored rather than trusted, so it cannot be used to file a
     * register under a colleague's name. An admin keeps the explicit field, since they are
     * recording on someone else's behalf by definition.
     */
    private Teacher resolveRegisterAuthor(Long requestedTeacherId, Long schoolId) {
        return portalAccess.currentTeacherOrEmpty()
                .orElseGet(() -> resolveRecordedBy(requestedTeacherId, schoolId));
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
