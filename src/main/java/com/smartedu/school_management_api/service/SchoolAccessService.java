package com.smartedu.school_management_api.service;

import com.smartedu.school_management_api.entity.School;
import com.smartedu.school_management_api.entity.User;
import com.smartedu.school_management_api.entity.UserRole;
import com.smartedu.school_management_api.exception.AccessDeniedAppException;
import com.smartedu.school_management_api.exception.BadRequestException;
import com.smartedu.school_management_api.exception.NotFoundException;
import com.smartedu.school_management_api.repository.SchoolRepository;
import com.smartedu.school_management_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The single gate for multi-tenant access decisions.
 *
 * <p>Every academic service routes reads and writes through here rather than
 * re-deriving the rules, which is what makes tenant isolation auditable in one place:
 *
 * <ul>
 *   <li>{@code SUPER_ADMIN} — sees all schools, must name a school when writing.</li>
 *   <li>{@code SCHOOL_ADMIN} — pinned to its own school; a school id in the request
 *       body is ignored rather than trusted, so it cannot be used to cross tenants.</li>
 *   <li>Everyone else — no academic write access at all.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class SchoolAccessService {

    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;

    /** The authenticated user, with {@code school} eagerly loaded. */
    @Transactional(readOnly = true)
    public User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            throw new AccessDeniedAppException("You must be signed in to perform this action");
        }
        return userRepository.findWithSchoolByUsernameIgnoreCase(authentication.getName())
                .orElseThrow(() -> new AccessDeniedAppException("Authenticated user no longer exists"));
    }

    public void requireAcademicManager(User user) {
        if (user.getRole() == null || !user.getRole().isAcademicManager()) {
            throw new AccessDeniedAppException("You do not have permission to manage academic data");
        }
    }

    /**
     * Resolves the school a write should land in.
     *
     * @param requestedSchoolId school from the request body; ignored for a school admin
     */
    @Transactional(readOnly = true)
    public School resolveWritableSchool(Long requestedSchoolId) {
        User currentUser = currentUser();
        requireAcademicManager(currentUser);

        if (currentUser.getRole() == UserRole.SCHOOL_ADMIN) {
            School own = currentUser.getSchool();
            if (own == null) {
                throw new BadRequestException("Your account is not linked to a school yet. Contact a super admin.");
            }
            return own;
        }

        if (requestedSchoolId == null) {
            throw new BadRequestException("School is required");
        }
        return schoolRepository.findById(requestedSchoolId)
                .orElseThrow(() -> NotFoundException.of("School", requestedSchoolId));
    }

    /** Throws unless the caller may touch data belonging to {@code schoolId}. */
    public void requireSchoolAccess(Long schoolId) {
        User currentUser = currentUser();
        requireAcademicManager(currentUser);

        if (currentUser.getRole() == UserRole.SUPER_ADMIN) {
            return;
        }
        Long own = currentUser.schoolIdOrNull();
        if (own == null || schoolId == null || !own.equals(schoolId)) {
            throw new AccessDeniedAppException("You cannot access another school's data");
        }
    }

    /**
     * The school filter for list queries: {@code null} for a super admin (meaning
     * "no filter"), otherwise the caller's own school id.
     */
    @Transactional(readOnly = true)
    public Long schoolScopeForCurrentUser() {
        User currentUser = currentUser();
        requireAcademicManager(currentUser);

        if (currentUser.getRole() == UserRole.SUPER_ADMIN) {
            return null;
        }
        Long own = currentUser.schoolIdOrNull();
        if (own == null) {
            throw new BadRequestException("Your account is not linked to a school yet. Contact a super admin.");
        }
        return own;
    }
}
