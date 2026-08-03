package com.smartedu.school_management_api.service;

import com.smartedu.school_management_api.dto.attendance.AttendanceBulkRequest;
import com.smartedu.school_management_api.dto.attendance.AttendanceRequest;
import com.smartedu.school_management_api.dto.attendance.AttendanceResponse;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {

    List<AttendanceResponse> getAllAttendance();

    AttendanceResponse getAttendanceById(Long id);

    /** The register for one class on one date; empty when it has not been taken. */
    List<AttendanceResponse> getRegister(Long classroomId, LocalDate date);

    AttendanceResponse createAttendance(AttendanceRequest request);

    AttendanceResponse updateAttendance(Long id, AttendanceRequest request);

    /** Saves a whole register, upserting on (student, date). Returns the saved rows. */
    List<AttendanceResponse> saveRegister(AttendanceBulkRequest request);

    void deleteAttendance(Long id);
}
