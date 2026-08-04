package com.smartedu.school_management_api.service.impl;

import com.smartedu.school_management_api.dto.about.AboutPageRequest;
import com.smartedu.school_management_api.dto.about.AboutPageResponse;
import com.smartedu.school_management_api.entity.AboutPage;
import com.smartedu.school_management_api.entity.User;
import com.smartedu.school_management_api.repository.AboutPageRepository;
import com.smartedu.school_management_api.service.AboutService;
import com.smartedu.school_management_api.service.SchoolAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

/**
 * The About page.
 *
 * <p>Reading is public, writing is a super admin's. That split is the whole feature: the
 * page is platform content, so it sits with schools and administrator accounts rather
 * than with anything a school admin owns.
 */
@Service
@RequiredArgsConstructor
public class AboutServiceImpl implements AboutService {

    private static final DateTimeFormatter LAST_UPDATED = DateTimeFormatter.ofPattern("d MMM yyyy");

    private static final String DEFAULT_TITLE = "About SmartEdu";
    private static final String DEFAULT_TAGLINE = "School management, simplified.";
    private static final String DEFAULT_BODY = """
            SmartEdu brings enrolment, attendance, examinations and fees together in one \
            place, so staff spend their time teaching rather than reconciling \
            spreadsheets. Each school manages its own students and staff, while the \
            platform keeps every school's records separate and secure.""";
    private static final String DEFAULT_MISSION = """
            To give every school dependable tools for the everyday work of running a \
            school, and to keep families informed about their children's progress.""";

    private final AboutPageRepository aboutPageRepository;
    private final SchoolAccessService access;

    /**
     * Serves the stored content, falling back to defaults.
     *
     * <p>No access check and no write: a visitor who has not signed in reads this, and a
     * page view should never create a row. The defaults exist so a fresh installation
     * shows something sensible instead of an empty screen.
     */
    @Override
    @Transactional(readOnly = true)
    public AboutPageResponse getAboutPage() {
        return aboutPageRepository.findById(AboutPage.SINGLETON_ID)
                .map(this::toResponse)
                .orElseGet(AboutServiceImpl::defaultResponse);
    }

    @Override
    @Transactional
    public AboutPageResponse updateAboutPage(AboutPageRequest request) {
        User currentUser = access.currentUser();
        access.requirePlatformAdmin(currentUser);

        // Created on first save with the fixed id, so editing repeatedly updates the
        // same row rather than inserting a new one each time.
        AboutPage page = aboutPageRepository.findById(AboutPage.SINGLETON_ID)
                .orElseGet(() -> AboutPage.builder().id(AboutPage.SINGLETON_ID).build());

        page.setTitle(request.title().trim());
        page.setTagline(trimToNull(request.tagline()));
        page.setBody(trimToNull(request.body()));
        page.setMission(trimToNull(request.mission()));
        page.setContactEmail(trimToNull(request.contactEmail()));
        page.setContactPhone(trimToNull(request.contactPhone()));
        page.setAddress(trimToNull(request.address()));
        page.setWebsite(trimToNull(request.website()));
        page.setUpdatedBy(currentUser.getFullName() != null
                ? currentUser.getFullName()
                : currentUser.getUsername());

        return toResponse(aboutPageRepository.save(page));
    }

    private AboutPageResponse toResponse(AboutPage page) {
        return new AboutPageResponse(
                page.getTitle(),
                page.getTagline(),
                page.getBody(),
                page.getMission(),
                page.getContactEmail(),
                page.getContactPhone(),
                page.getAddress(),
                page.getWebsite(),
                page.getUpdatedAt() != null ? LAST_UPDATED.format(page.getUpdatedAt()) : null,
                page.getUpdatedBy());
    }

    /** Shown until a super admin saves the page for the first time. */
    private static AboutPageResponse defaultResponse() {
        return new AboutPageResponse(
                DEFAULT_TITLE,
                DEFAULT_TAGLINE,
                DEFAULT_BODY,
                DEFAULT_MISSION,
                null, null, null, null, null, null);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
