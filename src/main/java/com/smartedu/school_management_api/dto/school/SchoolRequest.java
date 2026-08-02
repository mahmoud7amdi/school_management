package com.smartedu.school_management_api.dto.school;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Create/update payload for a school. Used for both POST and PUT. */
public record SchoolRequest(
        @NotBlank(message = "School name is required")
        @Size(max = 150, message = "School name must not exceed 150 characters")
        String name,

        @Size(max = 255, message = "Address must not exceed 255 characters")
        String address,

        @Size(max = 30, message = "Phone number must not exceed 30 characters")
        String phoneNumber,

        @Email(message = "Please enter a valid email")
        @Size(max = 150)
        String email,

        @Size(max = 500)
        String logoUrl,

        @Pattern(regexp = "^$|^(https?://).+", message = "Website must start with http:// or https://")
        @Size(max = 255)
        String website,

        Boolean active
) {
}
