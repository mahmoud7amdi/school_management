package com.smartedu.school_management_api.service.impl;
import com.smartedu.school_management_api.entity.School;
import com.smartedu.school_management_api.repository.SchoolRepository;
import com.smartedu.school_management_api.service.SchoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SchoolServiceImpl implements SchoolService {

    private final SchoolRepository schoolRepository;

    @Override
    @Transactional
    public School createSchool(School school) {
        if (schoolRepository.existsByName(school.getName())) {
            throw new RuntimeException("A school with this name already exists");
        }
        return schoolRepository.save(school);
    }

    @Override
    public List<School> getAllSchools() {
        return schoolRepository.findAll();
    }

    @Override
    public School getSchoolById(Long id) {
        return schoolRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("School not found with ID: " + id));
    }

    @Override
    @Transactional
    public School updateSchool(Long id, School schoolDetails) {
        School existingSchool = getSchoolById(id);

        if (schoolDetails.getName() != null && !schoolDetails.getName().isBlank()) {
            if (!existingSchool.getName().equals(schoolDetails.getName()) && schoolRepository.existsByName(schoolDetails.getName())) {
                throw new RuntimeException("A school with this name already exists");
            }
            existingSchool.setName(schoolDetails.getName());
        }
        
        if (schoolDetails.getAddress() != null) {
            existingSchool.setAddress(schoolDetails.getAddress());
        }
        
        if (schoolDetails.getPhoneNumber() != null) {
            existingSchool.setPhoneNumber(schoolDetails.getPhoneNumber());
        }
        
        if (schoolDetails.getEmail() != null) {
            existingSchool.setEmail(schoolDetails.getEmail());
        }
        
        if (schoolDetails.getWebsite() != null) {
            existingSchool.setWebsite(schoolDetails.getWebsite());
        }
        
        if (schoolDetails.getLogoUrl() != null) {
            existingSchool.setLogoUrl(schoolDetails.getLogoUrl());
        }

        return schoolRepository.save(existingSchool);
    }

    @Override
    @Transactional
    public void deleteSchool(Long id) {
        if (!schoolRepository.existsById(id)) {
            throw new RuntimeException("Cannot delete: School not found");
        }
        schoolRepository.deleteById(id);
    }
}
