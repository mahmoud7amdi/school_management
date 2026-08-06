package com.smartedu.school_management_api.dto.parent;

import com.smartedu.school_management_api.dto.user.AccountCredentials;
import com.smartedu.school_management_api.entity.GuardianRelationship;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Payload for creating or updating a parent/guardian.
 *
 * <p>The school is derived from the caller's tenant, never from the body — the same rule
 * the teacher payload follows. {@code children} replaces the whole set of links on update,
 * so an omitted child is unlinked.
 *
 * <p>{@code account} is used on create only, where it provisions the guardian's login in the
 * same transaction. On update it is ignored, so re-linking children never rewrites
 * credentials.
 */
public record ParentRequest(
        @NotBlank(message = "First name is required")
        @Size(max = 60, message = "First name must not exceed 60 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 60, message = "Last name must not exceed 60 characters")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email")
        @Size(max = 150)
        String email,

        @Size(max = 30, message = "Phone number must not exceed 30 characters")
        String phoneNumber,

        @Size(max = 120, message = "Occupation must not exceed 120 characters")
        String occupation,

        @Size(max = 255)
        String address,

        /** Sign-in details for the account created with this guardian. Create only. */
        @Valid
        AccountCredentials account,

        @Valid
        List<ChildLink> children
) {

    /** One parent-to-student link, carrying the relationship for that pair. */
    public record ChildLink(
            @NotNull(message = "Student is required")
            Long studentId,

            @NotNull(message = "Relationship is required")
            GuardianRelationship relationship,

            Boolean primaryContact
    ) {
    }
}
