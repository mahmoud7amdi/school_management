package com.smartedu.school_management_api.mapper;

import com.smartedu.school_management_api.dto.school.SchoolResponse;
import com.smartedu.school_management_api.entity.School;
import org.springframework.stereotype.Component;

@Component
public class SchoolMapper {

    public SchoolResponse toResponse(School school) {
        if (school == null) {
            return null;
        }
        return new SchoolResponse(
                school.getId(),
                school.getName(),
                school.getAddress(),
                school.getPhoneNumber(),
                school.getEmail(),
                school.getLogoUrl(),
                school.getWebsite(),
                school.getActive(),
                school.getCreatedAt(),
                school.getUpdatedAt());
    }
}
