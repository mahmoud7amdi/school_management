package com.smartedu.school_management_api.controller;

import com.smartedu.school_management_api.dto.ApiResponse;
import com.smartedu.school_management_api.dto.LoginResponse;
import com.smartedu.school_management_api.entity.User;
import com.smartedu.school_management_api.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Create user (Super Admin for School Admins, School Admin for Teachers/Students)
    @PreAuthorize("hasAuthority('SCHOOL_ADMIN') or hasAuthority('SUPER_ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<User>> createUser(@Valid @RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(HttpStatus.CREATED.value(), "User created successfully", createdUser, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Failed to create user", null, e.getMessage()));
        }
    }

    // Login - Open to everyone
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");
            LoginResponse response = userService.login(username, password);
            return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Logged in successfully", response, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(HttpStatus.UNAUTHORIZED.value(), "Login failed", null, e.getMessage()));
        }
    }

    // Logout
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(HttpServletRequest request) {
        try {
            userService.logout(request);
            return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Logged out successfully", "Logged out", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Logout failed", null, e.getMessage()));
        }
    }
    
    // Get current user profile
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> getCurrentUserProfile() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            User currentUser = userService.getUserByUsername(currentUsername);
            return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "Profile fetched successfully", currentUser, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(HttpStatus.UNAUTHORIZED.value(), "Unauthorized or invalid token", null, e.getMessage()));
        }
    }

    // Get user profile by username (Admin or self)
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'SCHOOL_ADMIN') or #username == authentication.name")
    @GetMapping("/profile/username/{username}")
    public ResponseEntity<ApiResponse<User>> getUserProfileByUsername(@PathVariable String username) {
        try {
            User user = userService.getUserByUsername(username);
            return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "User data fetched successfully", user, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "User not found", null, e.getMessage()));
        }
    }

    // Get user profile by ID (Admin only)
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'SCHOOL_ADMIN')")
    @GetMapping("/profile/id/{id}")
    public ResponseEntity<ApiResponse<User>> getUserProfileById(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);
            return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "User data fetched successfully", user, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(HttpStatus.NOT_FOUND.value(), "User not found", null, e.getMessage()));
        }
    }

    // Get all users (Super Admin & School Admin)
    @PreAuthorize("hasAuthority('SCHOOL_ADMIN') or hasAuthority('SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "All users fetched successfully", users, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error fetching users", null, e.getMessage()));
        }
    }

    // Update user - PUT (Super Admin & School Admin)
    @PreAuthorize("hasAuthority('SCHOOL_ADMIN') or hasAuthority('SUPER_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            User updatedUser = userService.updateUser(id, user);
            return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "User data updated successfully", updatedUser, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Failed to update user data", null, e.getMessage()));
        }
    }

    // Delete user - DELETE (Super Admin & School Admin)
    @PreAuthorize("hasAuthority('SCHOOL_ADMIN') or hasAuthority('SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK.value(), "User deleted successfully", "Deleted", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), "Failed to delete user", null, e.getMessage()));
        }
    }
}
