package com.smartedu.school_management_api.dto.parent;

import com.smartedu.school_management_api.dto.common.ReferenceResponse;
import com.smartedu.school_management_api.entity.GuardianRelationship;

import java.time.LocalDateTime;
import java.util.List;

public record ParentResponse(
        Long id,
        String firstName,
        String lastName,
        String fullName,
        String email,
        String phoneNumber,
        String occupation,
        String address,
        ReferenceResponse school,
        boolean hasUserAccount,
        List<ChildResponse> children,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    /** A linked child, flattened with the relationship that links them. */
    public record ChildResponse(
            Long studentId,
            String studentName,
            String admissionNumber,
            ReferenceResponse grade,
            ReferenceResponse classroom,
            GuardianRelationship relationship,
            String relationshipLabel,
            boolean primaryContact
    ) {
    }
}
