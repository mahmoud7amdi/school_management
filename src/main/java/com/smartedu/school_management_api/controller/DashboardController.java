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

    // --- Schools -----------------------------------------------------------
    @GetMapping("/dashboard/schools/all")
    public String allSchools() {
        return "schools/all-schools";
    }

    @GetMapping("/dashboard/schools/add")
    public String addSchool() {
        return "schools/add-school";
    }

    // --- Users -------------------------------------------------------------
    @GetMapping("/dashboard/users/all")
    public String allUsers() {
        return "users/all-users";
    }

    @GetMapping("/dashboard/users/add")
    public String addUser() {
        return "users/add-user";
    }

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
}
