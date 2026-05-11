package com.smartedu.school_management_api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard/index")
    public String dashboard() {
        // This will resolve to src/main/resources/templates/index.html
        return "index";
    }
}