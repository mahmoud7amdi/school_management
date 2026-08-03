package com.smartedu.school_management_api.service;

import com.smartedu.school_management_api.dto.auth.LoginResponse;
import com.smartedu.school_management_api.dto.user.CreateUserRequest;
import com.smartedu.school_management_api.dto.user.UpdateProfileRequest;
import com.smartedu.school_management_api.dto.user.UpdateUserRequest;
import com.smartedu.school_management_api.dto.user.UserResponse;
import com.smartedu.school_management_api.entity.UserRole;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.UUID;

public interface UserService {

    LoginResponse login(String username, String password);

    void logout(HttpServletRequest request);

    UserResponse createUser(CreateUserRequest request);

    /**
     * Users the caller may see. Both filters are optional; a school admin is always
     * pinned to its own school regardless of {@code schoolId}.
     */
    List<UserResponse> getAllUsers(Long schoolId, UserRole role);

    UserResponse getUserById(UUID id);

    UserResponse getCurrentUser();

    UserResponse updateUser(UUID id, UpdateUserRequest request);

    /** Self-service update for the signed-in user, whatever their role. */
    UserResponse updateOwnProfile(UpdateProfileRequest request);

    void deleteUser(UUID id);

    /** Teachers in the caller's school, for the classroom homeroom picker. */
    List<UserResponse> getAssignableTeachers();

    /** Logins of one role in the caller's school, for the account-link pickers. */
    List<UserResponse> getAssignableByRole(UserRole role);
}
