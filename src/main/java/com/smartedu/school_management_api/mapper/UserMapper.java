package com.smartedu.school_management_api.mapper;

import com.smartedu.school_management_api.dto.common.ReferenceResponse;
import com.smartedu.school_management_api.dto.user.UserResponse;
import com.smartedu.school_management_api.entity.School;
import com.smartedu.school_management_api.entity.User;
import org.springframework.stereotype.Component;

/**
 * Entity to DTO conversion for users.
 *
 * <p>Must be called inside the service transaction: it touches the lazy {@code school}
 * association, which repositories pre-load via {@code @EntityGraph}.
 */
@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        School school = user.getSchool();
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getAvatarUrl(),
                user.getRole(),
                user.getRole() != null ? user.getRole().getLabel() : null,
                user.getActive(),
                school != null ? ReferenceResponse.of(school.getId(), school.getName()) : null,
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
