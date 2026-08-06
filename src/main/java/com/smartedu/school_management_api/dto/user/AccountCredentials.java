package com.smartedu.school_management_api.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Sign-in details for the account created alongside a teacher, student or parent.
 *
 * <p>Every person record now owns a login, provisioned in the same transaction as the record
 * itself. This block carries the only two things that cannot be derived from the person —
 * their username and their initial password — so the three request payloads embed it rather
 * than repeating the constraints.
 *
 * <p>The remaining account columns are taken from the person: full name from their first and
 * last name, email and phone from their contact details.
 */
public record AccountCredentials(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,

        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
        String password,

        /** Defaults to an enabled account when omitted; only a deliberate false disables it. */
        Boolean active
) {

    public boolean activeOrDefault() {
        return active == null || active;
    }
}
