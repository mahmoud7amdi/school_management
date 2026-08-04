package com.smartedu.school_management_api.service;

import com.smartedu.school_management_api.entity.School;
import com.smartedu.school_management_api.entity.User;
import com.smartedu.school_management_api.entity.UserRole;
import com.smartedu.school_management_api.exception.AccessDeniedAppException;
import com.smartedu.school_management_api.exception.BadRequestException;
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
 *   <li>{@code SCHOOL_ADMIN} — pinned to its own school; a school id in the request
 *       body is ignored rather than trusted, so it cannot be used to cross tenants.</li>
 *   <li>{@code SUPER_ADMIN} — <strong>no</strong> access to data inside a school. Its
 *       remit is the platform: schools, administrator appointments and reports. It reaches
 *       a {@link School} record itself through {@link #requireSchoolVisible}, but never the
 *       students, staff or academic records within one.</li>
 *   <li>Everyone else — no academic access at all.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class SchoolAccessService {

    private final UserRepository userRepository;

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

    /**
     * Gate for school-scoped work. Denies a super admin as well as the portal roles.
     *
     * @see UserRole#isAcademicManager()
     */
    public void requireAcademicManager(User user) {
        if (user.getRole() == null || !user.getRole().isAcademicManager()) {
            throw new AccessDeniedAppException("You do not have permission to manage academic data");
        }
    }

    /** Gate for platform-level work: schools, administrator accounts, reports. */
    public void requirePlatformAdmin(User user) {
        if (user.getRole() == null || !user.getRole().isPlatformAdmin()) {
            throw new AccessDeniedAppException("Only a super admin can manage the platform");
        }
    }

    /**
     * Resolves the school a write should land in.
     *
     * <p>The caller is always a school admin by the time this returns, so the school is
     * its own. The parameter is kept because callers pass a school id from the request
     * body, and ignoring it here — rather than at each call site — is what stops it being
     * used to cross tenants.
     *
     * @param requestedSchoolId school from the request body; always ignored
     */
    @Transactional(readOnly = true)
    public School resolveWritableSchool(Long requestedSchoolId) {
        User currentUser = currentUser();
        requireAcademicManager(currentUser);

        School own = currentUser.getSchool();
        if (own == null) {
            throw new BadRequestException("Your account is not linked to a school yet. Contact a super admin.");
        }
        return own;
    }

    /** Throws unless the caller may touch data belonging to {@code schoolId}. */
    public void requireSchoolAccess(Long schoolId) {
        User currentUser = currentUser();
        requireAcademicManager(currentUser);

        Long own = currentUser.schoolIdOrNull();
        if (own == null || schoolId == null || !own.equals(schoolId)) {
            throw new AccessDeniedAppException("You cannot access another school's data");
        }
    }

    /**
     * Throws unless the caller may read the {@link School} record itself.
     *
     * <p>Wider than {@link #requireSchoolAccess}: a super admin manages the school
     * register, so it reads any school's own row while still being denied everything
     * inside that school.
     */
    @Transactional(readOnly = true)
    public void requireSchoolVisible(Long schoolId) {
        User currentUser = currentUser();

        if (currentUser.getRole() != null && currentUser.getRole().isPlatformAdmin()) {
            return;
        }
        requireSchoolAccess(schoolId);
    }

    /**
     * The school filter for list queries: always the caller's own school id, since a
     * super admin never reaches these queries.
     */
    @Transactional(readOnly = true)
    public Long schoolScopeForCurrentUser() {
        User currentUser = currentUser();
        requireAcademicManager(currentUser);

        Long own = currentUser.schoolIdOrNull();
        if (own == null) {
            throw new BadRequestException("Your account is not linked to a school yet. Contact a super admin.");
        }
        return own;
    }
}
