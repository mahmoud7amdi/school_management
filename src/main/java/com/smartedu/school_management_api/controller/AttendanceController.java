package com.smartedu.school_management_api.controller;

import com.smartedu.school_management_api.dto.ApiResponse;
import com.smartedu.school_management_api.dto.attendance.AttendanceBulkRequest;
import com.smartedu.school_management_api.dto.attendance.AttendanceRequest;
import com.smartedu.school_management_api.dto.attendance.AttendanceResponse;
import com.smartedu.school_management_api.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'SCHOOL_ADMIN')")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getAllAttendance() {
        return ResponseEntity.ok(ApiResponse.ok(attendanceService.getAllAttendance(), "Attendance loaded"));
    }

    /**
     * The register for one class on one date; empty when it has not been taken yet.
     *
     * <p>Open to teachers as well as admins. The service authorises the specific class,
     * so a teacher can only load a register for a class they are assigned to.
     */
    @GetMapping("/register")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'SCHOOL_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> getRegister(
            @RequestParam Long classroomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiResponse.ok(attendanceService.getRegister(classroomId, date),
                "Register loaded"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AttendanceResponse>> getAttendanceById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(attendanceService.getAttendanceById(id), "Attendance loaded"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AttendanceResponse>> createAttendance(
            @Valid @RequestBody AttendanceRequest request) {
        AttendanceResponse created = attendanceService.createAttendance(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(created, "Attendance recorded successfully"));
    }

    /**
     * Saves a whole register in one call, upserting on (student, date).
     *
     * <p>Taking the register is a teacher's job, so this is open to them for their own
     * classes. A teacher's register is always attributed to them: the service ignores
     * {@code recordedById} from the body rather than trusting it.
     */
    @PostMapping("/register")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'SCHOOL_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<List<AttendanceResponse>>> saveRegister(
            @Valid @RequestBody AttendanceBulkRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(attendanceService.saveRegister(request),
                "Register saved successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AttendanceResponse>> updateAttendance(
            @PathVariable Long id, @Valid @RequestBody AttendanceRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(attendanceService.updateAttendance(id, request),
                "Attendance updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteAttendance(@PathVariable Long id) {
        attendanceService.deleteAttendance(id);
        return ResponseEntity.ok(ApiResponse.ok("Deleted", "Attendance deleted successfully"));
    }
}
