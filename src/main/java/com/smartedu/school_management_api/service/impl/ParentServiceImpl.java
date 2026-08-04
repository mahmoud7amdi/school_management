package com.smartedu.school_management_api.service.impl;

import com.smartedu.school_management_api.dto.parent.ParentRequest;
import com.smartedu.school_management_api.dto.parent.ParentResponse;
import com.smartedu.school_management_api.entity.Parent;
import com.smartedu.school_management_api.entity.School;
import com.smartedu.school_management_api.entity.Student;
import com.smartedu.school_management_api.entity.StudentGuardian;
import com.smartedu.school_management_api.entity.User;
import com.smartedu.school_management_api.entity.UserRole;
import com.smartedu.school_management_api.exception.BadRequestException;
import com.smartedu.school_management_api.exception.DuplicateResourceException;
import com.smartedu.school_management_api.exception.NotFoundException;
import com.smartedu.school_management_api.mapper.ParentMapper;
import com.smartedu.school_management_api.repository.ParentRepository;
import com.smartedu.school_management_api.repository.StudentGuardianRepository;
import com.smartedu.school_management_api.repository.StudentRepository;
import com.smartedu.school_management_api.repository.UserRepository;
import com.smartedu.school_management_api.service.ParentService;
import com.smartedu.school_management_api.service.SchoolAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Guardian records and the links to their children.
 *
 * <p>Follows {@link TeacherServiceImpl}: the school comes from the caller's tenant via
 * {@link SchoolAccessService#resolveWritableSchool}, because a guardian has no parent
 * record to inherit one from.
 */
@Service
@RequiredArgsConstructor
public class ParentServiceImpl implements ParentService {

    private final ParentRepository parentRepository;
    private final StudentGuardianRepository studentGuardianRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final SchoolAccessService access;
    private final ParentMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<ParentResponse> getAllParents() {
        Long schoolId = access.schoolScopeForCurrentUser();
        return parentRepository.findBySchoolIdOrderByLastNameAscFirstNameAsc(schoolId).stream()
                .map(parent -> mapper.toResponse(parent, childLinks(parent.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ParentResponse getParentById(Long id) {
        Parent parent = loadAccessible(id);
        return mapper.toResponse(parent, childLinks(id));
    }

    @Override
    @Transactional
    public ParentResponse createParent(ParentRequest request) {
        School school = access.resolveWritableSchool(null);
        String email = normalizeEmail(request.email());

        if (email != null && parentRepository.existsBySchoolIdAndEmailIgnoreCase(school.getId(), email)) {
            throw new DuplicateResourceException(
                    "A parent with email '" + email + "' already exists in this school");
        }

        Parent parent = Parent.builder()
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .email(email)
                .phoneNumber(trimToNull(request.phoneNumber()))
                .occupation(trimToNull(request.occupation()))
                .address(trimToNull(request.address()))
                .userAccount(resolveUserAccount(request.userAccountId(), school.getId(), null))
                .school(school)
                .build();

        Parent saved = parentRepository.save(parent);
        replaceChildLinks(saved, request.children());

        return mapper.toResponse(saved, childLinks(saved.getId()));
    }

    @Override
    @Transactional
    public ParentResponse updateParent(Long id, ParentRequest request) {
        Parent parent = loadAccessible(id);
        Long schoolId = parent.getSchool().getId();
        String email = normalizeEmail(request.email());

        if (email != null
                && parentRepository.existsBySchoolIdAndEmailIgnoreCaseAndIdNot(schoolId, email, id)) {
            throw new DuplicateResourceException(
                    "A parent with email '" + email + "' already exists in this school");
        }

        parent.setFirstName(request.firstName().trim());
        parent.setLastName(request.lastName().trim());
        parent.setEmail(email);
        parent.setPhoneNumber(trimToNull(request.phoneNumber()));
        parent.setOccupation(trimToNull(request.occupation()));
        parent.setAddress(trimToNull(request.address()));
        parent.setUserAccount(resolveUserAccount(request.userAccountId(), schoolId, id));

        Parent saved = parentRepository.save(parent);
        replaceChildLinks(saved, request.children());

        return mapper.toResponse(saved, childLinks(saved.getId()));
    }

    @Override
    @Transactional
    public void deleteParent(Long id) {
        Parent parent = loadAccessible(id);
        // The links are this record's own rows and carry no history worth keeping, so
        // they go with it rather than blocking the delete.
        studentGuardianRepository.deleteByParentId(id);
        parentRepository.delete(parent);
    }

    // ------------------------------------------------------------------ helpers

    private Parent loadAccessible(Long id) {
        Parent parent = parentRepository.findWithRelationsById(id)
                .orElseThrow(() -> NotFoundException.of("Parent", id));
        access.requireSchoolAccess(parent.getSchool().getId());
        return parent;
    }

    private List<StudentGuardian> childLinks(Long parentId) {
        return studentGuardianRepository
                .findByParentIdOrderByStudentLastNameAscStudentFirstNameAsc(parentId);
    }

    /**
     * Rewrites the whole set of child links to match the request.
     *
     * <p>Replace rather than merge: the form posts the complete list, so a child left out
     * is an unlink. A null {@code children} means "not supplied" and leaves the existing
     * links alone, which keeps a partial update from silently orphaning a family.
     */
    private void replaceChildLinks(Parent parent, List<ParentRequest.ChildLink> children) {
        if (children == null) {
            return;
        }

        Long schoolId = parent.getSchool().getId();
        Set<Long> seen = new HashSet<>();
        List<StudentGuardian> links = new ArrayList<>();

        for (ParentRequest.ChildLink child : children) {
            if (!seen.add(child.studentId())) {
                throw new BadRequestException("The same child is listed twice");
            }

            Student student = studentRepository.findWithRelationsById(child.studentId())
                    .orElseThrow(() -> NotFoundException.of("Student", child.studentId()));
            if (!student.getSchool().getId().equals(schoolId)) {
                throw new BadRequestException(
                        "Student '" + student.getFullName() + "' belongs to a different school");
            }

            links.add(StudentGuardian.builder()
                    .parent(parent)
                    .student(student)
                    .relationship(child.relationship())
                    .primaryContact(Boolean.TRUE.equals(child.primaryContact()))
                    .build());
        }

        studentGuardianRepository.deleteByParentId(parent.getId());
        // Flush the deletes before inserting, or the (parent, student) unique index can
        // reject a row that is only being re-created.
        studentGuardianRepository.flush();
        studentGuardianRepository.saveAll(links);
    }

    /**
     * Links an optional login. The account must be a {@code PARENT} in the same school and
     * not already claimed by another guardian record — the teacher rule, one role across.
     */
    private User resolveUserAccount(UUID userAccountId, Long schoolId, Long currentParentId) {
        if (userAccountId == null) {
            return null;
        }
        User user = userRepository.findWithSchoolById(userAccountId)
                .orElseThrow(() -> NotFoundException.of("User", userAccountId));

        if (user.getRole() != UserRole.PARENT) {
            throw new BadRequestException("The selected user account is not a parent");
        }
        if (user.schoolIdOrNull() == null || !user.schoolIdOrNull().equals(schoolId)) {
            throw new BadRequestException("The selected user account belongs to a different school");
        }

        boolean claimedByAnother = parentRepository.findByUserAccountId(userAccountId)
                .filter(existing -> currentParentId == null || !existing.getId().equals(currentParentId))
                .isPresent();
        if (claimedByAnother) {
            throw new DuplicateResourceException("That user account is already linked to another parent");
        }
        return user;
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
