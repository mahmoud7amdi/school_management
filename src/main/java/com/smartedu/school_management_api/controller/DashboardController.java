package com.smartedu.school_management_api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * View resolution for the Thymeleaf dashboard.
 *
 * <p>These return page shells only; each one loads its data over the authenticated
 * API, so no model attributes are needed here.
 */
@Controller
public class DashboardController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping({"/", "/dashboard", "/dashboard/index", "/index.html"})
    public String dashboard() {
        return "index";
    }

    /** Public About Us page. No login: this is the one page a visitor can read. */
    @GetMapping("/about")
    public String about() {
        return "about";
    }

    /** Editor for the above. Content is saved through {@code PUT /api/v1/about}. */
    @GetMapping("/dashboard/about")
    public String editAbout() {
        return "about-edit";
    }

    // --- Schools -----------------------------------------------------------
    @GetMapping("/dashboard/schools/all")
    public String allSchools() {
        return "schools/all-schools";
    }

    @GetMapping("/dashboard/schools/add")
    public String addSchool() {
        return "schools/add-school";
    }

    /** Platform reports. Super admin only, enforced by {@code /api/v1/reports/**}. */
    @GetMapping("/dashboard/reports")
    public String reports() {
        return "reports/platform-report";
    }

    // --- Administrators ----------------------------------------------------
    // Super admin only, enforced by /api/v1/users/**. School admins are appointed here and
    // nowhere else; a school's own admin provisions teachers, students and parents through
    // their respective pages, which create the sign-in account along with the record.
    @GetMapping("/dashboard/admins/all")
    public String allAdmins() {
        return "admins/all-admins";
    }

    @GetMapping("/dashboard/admins/add")
    public String addAdmin() {
        return "admins/add-admin";
    }

    /** Self-service, for every role. Not part of administrator management. */
    @GetMapping("/dashboard/users/profile")
    public String profile() {
        return "users/profile";
    }

    // --- Students ----------------------------------------------------------
    @GetMapping("/dashboard/students/all")
    public String allStudents() {
        return "students/all-students";
    }

    @GetMapping("/dashboard/students/add")
    public String addStudent() {
        return "students/add-student";
    }

    // --- Parents -----------------------------------------------------------
    @GetMapping("/dashboard/parents/all")
    public String allParents() {
        return "parents/all-parents";
    }

    @GetMapping("/dashboard/parents/add")
    public String addParent() {
        return "parents/add-parent";
    }

    // --- Academics ---------------------------------------------------------
    @GetMapping("/dashboard/academic-years/all")
    public String allAcademicYears() {
        return "academic-years/all-academic-years";
    }

    @GetMapping("/dashboard/academic-years/add")
    public String addAcademicYear() {
        return "academic-years/add-academic-year";
    }

    @GetMapping("/dashboard/grades/all")
    public String allGrades() {
        return "grades/all-grades";
    }

    @GetMapping("/dashboard/grades/add")
    public String addGrade() {
        return "grades/add-grade";
    }

    @GetMapping("/dashboard/subjects/all")
    public String allSubjects() {
        return "subjects/all-subjects";
    }

    @GetMapping("/dashboard/subjects/add")
    public String addSubject() {
        return "subjects/add-subject";
    }

    @GetMapping("/dashboard/classrooms/all")
    public String allClassrooms() {
        return "classrooms/all-classrooms";
    }

    @GetMapping("/dashboard/classrooms/add")
    public String addClassroom() {
        return "classrooms/add-classroom";
    }

    @GetMapping("/dashboard/sections/all")
    public String allSections() {
        return "sections/all-sections";
    }

    @GetMapping("/dashboard/sections/add")
    public String addSection() {
        return "sections/add-section";
    }

    // --- Staff -------------------------------------------------------------
    @GetMapping("/dashboard/teachers/all")
    public String allTeachers() {
        return "teachers/all-teachers";
    }

    @GetMapping("/dashboard/teachers/add")
    public String addTeacher() {
        return "teachers/add-teacher";
    }

    // --- Enrollment --------------------------------------------------------
    @GetMapping("/dashboard/enrollments/all")
    public String allEnrollments() {
        return "enrollments/all-enrollments";
    }

    @GetMapping("/dashboard/enrollments/add")
    public String addEnrollment() {
        return "enrollments/add-enrollment";
    }

    // --- Attendance --------------------------------------------------------
    @GetMapping("/dashboard/attendance/all")
    public String allAttendance() {
        return "attendance/all-attendance";
    }

    /** The daily register. Picks its class and date client-side. */
    @GetMapping("/dashboard/attendance/register")
    public String attendanceRegister() {
        return "attendance/attendance-register";
    }

    // --- Exams -------------------------------------------------------------
    @GetMapping("/dashboard/exams/all")
    public String allExams() {
        return "exams/all-exams";
    }

    @GetMapping("/dashboard/exams/add")
    public String addExam() {
        return "exams/add-exam";
    }

    /** Marks entry. The exam is chosen client-side via {@code ?examId=}. */
    @GetMapping("/dashboard/exams/results")
    public String examResults() {
        return "exams/exam-results";
    }

    // --- Fees --------------------------------------------------------------
    @GetMapping("/dashboard/fees/structures")
    public String allFeeStructures() {
        return "fees/all-fee-structures";
    }

    @GetMapping("/dashboard/fees/structures/add")
    public String addFeeStructure() {
        return "fees/add-fee-structure";
    }

    @GetMapping("/dashboard/fees/payments")
    public String allFeePayments() {
        return "fees/all-payments";
    }

    @GetMapping("/dashboard/fees/payments/add")
    public String addFeePayment() {
        return "fees/add-payment";
    }

    // --- Self-service portal ------------------------------------------------
    // Shells only, like every other route here. Each one loads from /api/v1/portal/**,
    // which is where the role is actually enforced.

    @GetMapping("/dashboard/portal/classes")
    public String myClasses() {
        return "portal/teacher-classes";
    }

    @GetMapping("/dashboard/portal/children")
    public String myChildren() {
        return "portal/children";
    }

    /** Own attendance for a student; per-child for a parent, chosen client-side. */
    @GetMapping("/dashboard/portal/my-attendance")
    public String myAttendance() {
        return "portal/my-attendance";
    }

    @GetMapping("/dashboard/portal/my-results")
    public String myResults() {
        return "portal/my-results";
    }

    @GetMapping("/dashboard/portal/my-fees")
    public String myFees() {
        return "portal/my-fees";
    }

    /** Submission for a family, review queue for a teacher — one page, two modes. */
    @GetMapping("/dashboard/portal/absence-notes")
    public String absenceNotes() {
        return "portal/absence-notes";
    }
}
