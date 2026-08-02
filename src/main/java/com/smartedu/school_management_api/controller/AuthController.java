package com.smartedu.school_management_api.controller;

import com.smartedu.school_management_api.dto.ApiResponse;
import com.smartedu.school_management_api.dto.auth.LoginRequest;
import com.smartedu.school_management_api.dto.auth.LoginResponse;
import com.smartedu.school_management_api.dto.user.UpdateProfileRequest;
import com.smartedu.school_management_api.dto.user.UserResponse;
import com.smartedu.school_management_api.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication and self-service profile.
 *
 * <p>Separate from {@link UserController} because these routes are about the caller's
 * own account: login/logout are public, and {@code /me} is available to every role,
 * whereas user administration is restricted.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request.username(), request.password());
        return ResponseEntity.ok(ApiResponse.ok(response, "Signed in successfully"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
        userService.logout(request);
        return ResponseEntity.ok(ApiResponse.ok("Signed out", "Signed out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> currentUser() {
        return ResponseEntity.ok(ApiResponse.ok(userService.getCurrentUser(), "Profile loaded"));
    }

    /** Self-service update. Cannot change role, active state or school. */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateOwnProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(userService.updateOwnProfile(request), "Profile updated successfully"));
    }
}
