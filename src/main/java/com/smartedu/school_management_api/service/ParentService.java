package com.smartedu.school_management_api.service;

import com.smartedu.school_management_api.dto.parent.ParentRequest;
import com.smartedu.school_management_api.dto.parent.ParentResponse;

import java.util.List;

public interface ParentService {

    List<ParentResponse> getAllParents();

    ParentResponse getParentById(Long id);

    ParentResponse createParent(ParentRequest request);

    ParentResponse updateParent(Long id, ParentRequest request);

    void deleteParent(Long id);
}
