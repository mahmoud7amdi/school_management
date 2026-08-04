package com.smartedu.school_management_api.controller;

import com.smartedu.school_management_api.dto.ApiResponse;
import com.smartedu.school_management_api.dto.about.AboutPageRequest;
import com.smartedu.school_management_api.dto.about.AboutPageResponse;
import com.smartedu.school_management_api.service.AboutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The About Us page: public to read, super admin to edit.
 *
 * <p>No class-level {@code @PreAuthorize} here, unlike the other controllers, because the
 * two methods differ: the GET is deliberately open to visitors who have not signed in
 * (see the permitAll entry in {@code SecurityConfig}), and only the PUT is restricted.
 */
@RestController
@RequestMapping("/api/v1/about")
@RequiredArgsConstructor
public class AboutController {

    private final AboutService aboutService;

    /** Public. Returns built-in defaults until a super admin edits the page. */
    @GetMapping
    public ResponseEntity<ApiResponse<AboutPageResponse>> getAboutPage() {
        return ResponseEntity.ok(ApiResponse.ok(aboutService.getAboutPage(), "About page loaded"));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<AboutPageResponse>> updateAboutPage(
            @Valid @RequestBody AboutPageRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(aboutService.updateAboutPage(request), "About page updated"));
    }
}
