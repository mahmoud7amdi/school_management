package com.smartedu.school_management_api.service;

import com.smartedu.school_management_api.dto.section.SectionRequest;
import com.smartedu.school_management_api.dto.section.SectionResponse;

import java.util.List;

public interface SectionService {

    List<SectionResponse> getAllSections();

    SectionResponse getSectionById(Long id);

    SectionResponse createSection(SectionRequest request);

    SectionResponse updateSection(Long id, SectionRequest request);

    void deleteSection(Long id);
}
