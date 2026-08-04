package com.smartedu.school_management_api.service.impl;

import com.smartedu.school_management_api.dto.school.SchoolRequest;
import com.smartedu.school_management_api.dto.school.SchoolResponse;
import com.smartedu.school_management_api.entity.School;
import com.smartedu.school_management_api.entity.User;
import com.smartedu.school_management_api.entity.UserRole;
import com.smartedu.school_management_api.exception.BadRequestException;
import com.smartedu.school_management_api.exception.DuplicateResourceException;
import com.smartedu.school_management_api.exception.NotFoundException;
import com.smartedu.school_management_api.mapper.SchoolMapper;
import com.smartedu.school_management_api.repository.SchoolRepository;
import com.smartedu.school_management_api.repository.StudentRepository;
import com.smartedu.school_management_api.repository.UserRepository;
import com.smartedu.school_management_api.service.SchoolAccessService;
import com.smartedu.school_management_api.service.SchoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SchoolServiceImpl implements SchoolService {

    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final SchoolAccessService access;
    private final SchoolMapper schoolMapper;

    @Override
    @Transactional
    public SchoolResponse createSchool(SchoolRequest request) {
        String name = request.name().trim();
        if (schoolRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("A school named '" + name + "' already exists");
        }
        String email = normalizeEmail(request.email());
        if (email != null && schoolRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("A school with email '" + email + "' already exists");
        }

        School school = School.builder()
                .name(name)
                .address(trimToNull(request.address()))
                .phoneNumber(trimToNull(request.phoneNumber()))
                .email(email)
                .logoUrl(trimToNull(request.logoUrl()))
                .website(trimToNull(request.website()))
                .active(request.active() == null || request.active())
                .build();

        return schoolMapper.toResponse(schoolRepository.save(school));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SchoolResponse> getAllSchools() {
        User currentUser = access.currentUser();

        // A school admin sees exactly one school: its own. Anything wider would be a
        // tenant leak, and the picker on the dashboard relies on this shape.
        if (currentUser.getRole() == UserRole.SCHOOL_ADMIN) {
            Long schoolId = currentUser.schoolIdOrNull();
            if (schoolId == null) {
                return List.of();
            }
            return schoolRepository.findById(schoolId)
                    .map(school -> List.of(schoolMapper.toResponse(school)))
                    .orElseGet(List::of);
        }

        return schoolRepository.findAllByOrderByNameAsc().stream()
                .map(schoolMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SchoolResponse getSchoolById(Long id) {
        // The school register is platform data, so a super admin reads any row here.
        // requireSchoolAccess would deny it — that gate guards data *inside* a school.
        access.requireSchoolVisible(id);
        return schoolMapper.toResponse(loadSchool(id));
    }

    @Override
    @Transactional
    public SchoolResponse updateSchool(Long id, SchoolRequest request) {
        School school = loadSchool(id);

        String name = request.name().trim();
        if (schoolRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new DuplicateResourceException("A school named '" + name + "' already exists");
        }
        String email = normalizeEmail(request.email());
        if (email != null && schoolRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new DuplicateResourceException("A school with email '" + email + "' already exists");
        }

        school.setName(name);
        school.setAddress(trimToNull(request.address()));
        school.setPhoneNumber(trimToNull(request.phoneNumber()));
        school.setEmail(email);
        school.setLogoUrl(trimToNull(request.logoUrl()));
        school.setWebsite(trimToNull(request.website()));
        if (request.active() != null) {
            school.setActive(request.active());
        }

        return schoolMapper.toResponse(schoolRepository.save(school));
    }

    @Override
    @Transactional
    public void deleteSchool(Long id) {
        School school = loadSchool(id);

        // Academic records cascade away with the school, but user accounts do not:
        // deleting them would destroy logins that may be re-pointed elsewhere.
        long userCount = userRepository.countBySchoolId(id);
        if (userCount > 0) {
            throw new BadRequestException("This school still has " + userCount
                    + " user account(s). Reassign or delete them before deleting the school.");
        }

        // Students are guarded explicitly rather than left to the cascade. Hibernate
        // does not order the school's collections, so it may try to detach students
        // from their grade first — and students.grade_id is NOT NULL, which surfaces
        // as an opaque constraint error. Refusing here also makes wiping a whole
        // roster a deliberate act instead of a side effect of removing the school.
        long studentCount = studentRepository.countBySchoolId(id);
        if (studentCount > 0) {
            throw new BadRequestException("This school still has " + studentCount
                    + " enrolled student(s). Delete or transfer them before deleting the school.");
        }

        schoolRepository.delete(school);
    }

    private School loadSchool(Long id) {
        return schoolRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("School", id));
    }

    private static String normalizeEmail(String email) {
        String trimmed = trimToNull(email);
        return trimmed == null ? null : trimmed.toLowerCase();
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
