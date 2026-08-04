package com.smartedu.school_management_api.dto.about;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * New content for the About page. Super admin only.
 *
 * <p>Only the title is mandatory: a school that has not written its mission statement
 * yet should still be able to save a name and a phone number.
 */
public record AboutPageRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 150, message = "Title must not exceed 150 characters")
        String title,

        @Size(max = 300, message = "Tagline must not exceed 300 characters")
        String tagline,

        @Size(max = 20000, message = "Body is too long")
        String body,

        @Size(max = 20000, message = "Mission is too long")
        String mission,

        @Email(message = "Please enter a valid email")
        @Size(max = 150)
        String contactEmail,

        @Size(max = 30, message = "Phone number must not exceed 30 characters")
        String contactPhone,

        @Size(max = 255, message = "Address must not exceed 255 characters")
        String address,

        @Size(max = 255)
        String website
) {
}
