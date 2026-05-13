package com.smartedu.school_management_api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

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

    @GetMapping("/dashboard/schools/add")
    public String addSchool() {
        return "schools/add-school";
    }

    @GetMapping("/dashboard/schools/all")
    public String allSchools() {
        return "schools/all-schools";
    }

    @GetMapping("/dashboard/users/add")
    public String addUser() {
        return "users/add-user";
    }

    @GetMapping("/dashboard/users/all")
    public String allUsers() {
        return "users/all-users";
    }
}
