package com.smartedu.school_management_api.service;

import com.smartedu.school_management_api.dto.about.AboutPageRequest;
import com.smartedu.school_management_api.dto.about.AboutPageResponse;

public interface AboutService {

    /**
     * The About page content, or built-in defaults when it has never been edited.
     *
     * <p>Public: this is what an unauthenticated visitor reads.
     */
    AboutPageResponse getAboutPage();

    /** Replaces the content. Super admin only. */
    AboutPageResponse updateAboutPage(AboutPageRequest request);
}
