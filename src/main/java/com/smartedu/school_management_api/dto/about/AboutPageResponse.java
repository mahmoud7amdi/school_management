package com.smartedu.school_management_api.dto.about;

/**
 * The About page as served to visitors.
 *
 * @param lastUpdated formatted for display, or null when the page is still the default
 * @param updatedBy   the administrator who last edited it, for the editor screen
 */
public record AboutPageResponse(
        String title,
        String tagline,
        String body,
        String mission,
        String contactEmail,
        String contactPhone,
        String address,
        String website,
        String lastUpdated,
        String updatedBy
) {
}
