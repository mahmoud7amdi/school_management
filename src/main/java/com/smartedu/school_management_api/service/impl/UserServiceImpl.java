package com.smartedu.school_management_api.service.impl;

import com.smartedu.school_management_api.config.AdminProperties;
import com.smartedu.school_management_api.config.JwtProperties;
import com.smartedu.school_management_api.dto.auth.LoginResponse;
import com.smartedu.school_management_api.dto.user.CreateUserRequest;
import com.smartedu.school_management_api.dto.user.UpdateProfileRequest;
import com.smartedu.school_management_api.dto.user.UpdateUserRequest;
import com.smartedu.school_management_api.dto.user.UserResponse;
import com.smartedu.school_management_api.entity.School;
import com.smartedu.school_management_api.entity.TokenBlacklist;
import com.smartedu.school_management_api.entity.User;
import com.smartedu.school_management_api.entity.UserRole;
import com.smartedu.school_management_api.exception.AccessDeniedAppException;
import com.smartedu.school_management_api.exception.BadRequestException;
import com.smartedu.school_management_api.exception.DuplicateResourceException;
import com.smartedu.school_management_api.exception.NotFoundException;
import com.smartedu.school_management_api.mapper.UserMapper;
import com.smartedu.school_management_api.repository.SchoolRepository;
import com.smartedu.school_management_api.repository.ParentRepository;
import com.smartedu.school_management_api.repository.StudentRepository;
import com.smartedu.school_management_api.repository.TeacherRepository;
import com.smartedu.school_management_api.repository.TokenBlacklistRepository;
import com.smartedu.school_management_api.repository.UserRepository;
import com.smartedu.school_management_api.service.CustomUserDetailsService;
import com.smartedu.school_management_api.service.SchoolAccessService;
import com.smartedu.school_management_api.service.UserService;
import com.smartedu.school_management_api.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * User lifecycle and authentication.
 *
 * <p>Who may act on whom:
 * <ul>
 *   <li>{@code SUPER_ADMIN} manages {@code SCHOOL_ADMIN} accounts (and its own profile).</li>
 *   <li>{@code SCHOOL_ADMIN} manages teachers/students/parents inside its own school.</li>
 *   <li>Anyone may edit their own profile via {@link #updateOwnProfile}, which cannot
 *       change role, active state or school.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final ParentRepository parentRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;
    private final AdminProperties adminProperties;
    private final SchoolAccessService access;
    private final UserMapper userMapper;

    // ---------------------------------------------------------------- bootstrap

    /**
     * Creates the initial super admin when the table has none.
     *
     * <p>Keyed on "no SUPER_ADMIN exists" rather than on the username, so renaming the
     * bootstrap account does not silently resurrect a second one.
     */
    @Transactional
    public void initAdmin() {
        if (userRepository.countByRole(UserRole.SUPER_ADMIN) > 0) {
            return;
        }
        if (userRepository.existsByUsernameIgnoreCase(adminProperties.getUsername())) {
            return;
        }
        User admin = User.builder()
                .username(adminProperties.getUsername())
                .password(passwordEncoder.encode(adminProperties.getPassword()))
                .fullName(adminProperties.getFullName())
                .email(adminProperties.getEmail())
                .role(UserRole.SUPER_ADMIN)
                .active(true)
                .build();
        userRepository.save(admin);
        log.info("Created bootstrap super admin '{}'. Change its password immediately.", admin.getUsername());
    }

    // --------------------------------------------------------------------- auth

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(String username, String password) {
        // Throws AuthenticationException (bad credentials, or DisabledException for a
        // deactivated account); the global handler turns both into a 401.
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String token = jwtUtil.generateToken(userDetails);

        User user = userRepository.findWithSchoolByUsernameIgnoreCase(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));

        return LoginResponse.of(token, jwtProperties.getExpiration().toSeconds(), userMapper.toResponse(user));
    }

    @Override
    @Transactional
    public void logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            // Nothing to revoke — treat as success so the client can still clear state.
            return;
        }
        String jwt = header.substring(7).trim();
        if (jwt.isEmpty()) {
            return;
        }

        String hash = jwtUtil.hashToken(jwt);
        if (tokenBlacklistRepository.existsByTokenHash(hash)) {
            return;
        }
        try {
            Instant expiresAt = jwtUtil.extractExpiration(jwt).toInstant();
            tokenBlacklistRepository.save(TokenBlacklist.builder()
                    .tokenHash(hash)
                    .expiresAt(expiresAt)
                    .revokedAt(Instant.now())
                    .build());
        } catch (Exception ex) {
            // An unparseable token is already useless; no need to fail the logout.
            log.debug("Logout called with a token that could not be parsed: {}", ex.getMessage());
        }
    }

    // ------------------------------------------------------------------- create

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        User currentUser = access.currentUser();
        UserRole targetRole = request.role();

        School school = resolveSchoolForCreate(currentUser, targetRole, request.schoolId());

        if (userRepository.existsByUsernameIgnoreCase(request.username())) {
            throw new DuplicateResourceException("Username '" + request.username() + "' is already taken");
        }
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new DuplicateResourceException("Email '" + request.email() + "' is already registered");
        }

        User user = User.builder()
                .username(request.username().trim())
                .fullName(request.fullName().trim())
                .email(request.email().trim().toLowerCase())
                .password(passwordEncoder.encode(request.password()))
                .role(targetRole)
                .phoneNumber(trimToNull(request.phoneNumber()))
                .avatarUrl(trimToNull(request.avatarUrl()))
                .active(true)
                .school(school)
                .build();

        return userMapper.toResponse(userRepository.save(user));
    }

    /** Applies the role-creation matrix and returns the school the new user belongs to. */
    private School resolveSchoolForCreate(User currentUser, UserRole targetRole, Long requestedSchoolId) {
        if (currentUser.getRole() == UserRole.SUPER_ADMIN) {
            // A super admin may now provision any role directly, not just School Admins,
            // so it can stand in for a school with no working admin of its own.
            if (targetRole == UserRole.SUPER_ADMIN) {
                // A super admin has no school by design; every other role needs one.
                return null;
            }
            if (requestedSchoolId == null) {
                throw new BadRequestException("A school must be selected for a " + targetRole.getLabel());
            }
            return schoolRepository.findById(requestedSchoolId)
                    .orElseThrow(() -> NotFoundException.of("School", requestedSchoolId));
        }

        if (currentUser.getRole() == UserRole.SCHOOL_ADMIN) {
            if (targetRole == UserRole.SUPER_ADMIN || targetRole == UserRole.SCHOOL_ADMIN) {
                throw new AccessDeniedAppException("A school admin cannot create admin accounts");
            }
            School own = currentUser.getSchool();
            if (own == null) {
                throw new BadRequestException("Your account is not linked to a school yet. Contact a super admin.");
            }
            // The body's schoolId is ignored: a school admin can only ever create locally.
            return own;
        }

        throw new AccessDeniedAppException("You do not have permission to create users");
    }

    // --------------------------------------------------------------------- read

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers(Long schoolId, UserRole role) {
        User currentUser = access.currentUser();

        List<User> users = switch (currentUser.getRole()) {
            // Every account, so the Users page can act as the intervention surface the
            // widened loadManageableUser makes possible. Filters keep it navigable.
            case SUPER_ADMIN -> userRepository.search(schoolId, role);
            case SCHOOL_ADMIN -> {
                Long own = currentUser.schoolIdOrNull();
                if (own == null) {
                    throw new BadRequestException("Your account is not linked to a school yet.");
                }
                // The requested school is ignored rather than trusted: a school admin
                // is always pinned to its own, exactly as elsewhere.
                yield userRepository.searchSchoolMembers(own, role);
            }
            default -> throw new AccessDeniedAppException("You do not have permission to view users");
        };

        return users.stream().map(userMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        return userMapper.toResponse(loadManageableUser(id));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        return userMapper.toResponse(access.currentUser());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAssignableTeachers() {
        return getAssignableByRole(UserRole.TEACHER);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAssignableByRole(UserRole role) {
        User currentUser = access.currentUser();
        access.requireAcademicManager(currentUser);

        if (role == UserRole.SUPER_ADMIN || role == UserRole.SCHOOL_ADMIN) {
            // These pickers exist to link a domain record to a login, and no domain
            // record is ever owned by an admin account.
            throw new BadRequestException("Admin accounts cannot be linked to a person record");
        }

        Long schoolId = currentUser.getRole() == UserRole.SUPER_ADMIN
                ? null
                : currentUser.schoolIdOrNull();

        List<User> users = schoolId == null
                ? userRepository.findByRoleOrderByFullNameAsc(role)
                : userRepository.findBySchoolIdAndRoleOrderByFullNameAsc(schoolId, role);

        return users.stream().map(userMapper::toResponse).toList();
    }

    // ------------------------------------------------------------------- update

    @Override
    @Transactional
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        User user = loadManageableUser(id);
        User currentUser = access.currentUser();

        if (request.username() != null && !request.username().isBlank()) {
            String username = request.username().trim();
            if (userRepository.existsByUsernameIgnoreCaseAndIdNot(username, id)) {
                throw new DuplicateResourceException("Username '" + username + "' is already taken");
            }
            user.setUsername(username);
        }

        if (request.email() != null && !request.email().isBlank()) {
            String email = request.email().trim().toLowerCase();
            if (userRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
                throw new DuplicateResourceException("Email '" + email + "' is already registered");
            }
            user.setEmail(email);
        }

        if (request.fullName() != null && !request.fullName().isBlank()) {
            user.setFullName(request.fullName().trim());
        }
        if (request.phoneNumber() != null) {
            user.setPhoneNumber(trimToNull(request.phoneNumber()));
        }
        if (request.avatarUrl() != null) {
            user.setAvatarUrl(trimToNull(request.avatarUrl()));
        }
        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        if (request.role() != null && request.role() != user.getRole()) {
            assertRoleAssignable(currentUser, request.role());
            user.setRole(request.role());
        }

        if (request.active() != null) {
            if (user.getId().equals(currentUser.getId()) && !request.active()) {
                throw new BadRequestException("You cannot deactivate your own account");
            }
            user.setActive(request.active());
        }

        // Only a super admin can move a user between schools.
        if (request.schoolId() != null && currentUser.getRole() == UserRole.SUPER_ADMIN) {
            School school = schoolRepository.findById(request.schoolId())
                    .orElseThrow(() -> NotFoundException.of("School", request.schoolId()));
            user.setSchool(school);
        }

        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserResponse updateOwnProfile(UpdateProfileRequest request) {
        User user = access.currentUser();

        if (request.username() != null && !request.username().isBlank()) {
            String username = request.username().trim();
            if (userRepository.existsByUsernameIgnoreCaseAndIdNot(username, user.getId())) {
                throw new DuplicateResourceException("Username '" + username + "' is already taken");
            }
            user.setUsername(username);
        }

        if (request.email() != null && !request.email().isBlank()) {
            String email = request.email().trim().toLowerCase();
            if (userRepository.existsByEmailIgnoreCaseAndIdNot(email, user.getId())) {
                throw new DuplicateResourceException("Email '" + email + "' is already registered");
            }
            user.setEmail(email);
        }

        if (request.fullName() != null && !request.fullName().isBlank()) {
            user.setFullName(request.fullName().trim());
        }
        if (request.phoneNumber() != null) {
            user.setPhoneNumber(trimToNull(request.phoneNumber()));
        }
        if (request.avatarUrl() != null) {
            user.setAvatarUrl(trimToNull(request.avatarUrl()));
        }

        // Changing a password requires proving knowledge of the current one, so a
        // stolen token cannot be used to lock the real owner out.
        if (request.newPassword() != null && !request.newPassword().isBlank()) {
            if (request.currentPassword() == null || request.currentPassword().isBlank()) {
                throw new BadRequestException("Enter your current password to set a new one");
            }
            if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
                throw new BadRequestException("Your current password is incorrect");
            }
            user.setPassword(passwordEncoder.encode(request.newPassword()));
        }

        return userMapper.toResponse(userRepository.save(user));
    }

    // ------------------------------------------------------------------- delete

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        User user = loadManageableUser(id);
        User currentUser = access.currentUser();

        if (user.getId().equals(currentUser.getId())) {
            throw new BadRequestException("You cannot delete your own account");
        }
        // Student, teacher and parent rows all reference a login through userAccount.
        // The FK would block the delete anyway; naming the record makes the fix obvious.
        if (studentRepository.existsByUserAccountId(id)) {
            throw new BadRequestException(
                    "This account is linked to a student record. Unlink it before deleting the account.");
        }
        if (teacherRepository.existsByUserAccountId(id)) {
            throw new BadRequestException(
                    "This account is linked to a teacher record. Unlink it before deleting the account.");
        }
        if (parentRepository.existsByUserAccountId(id)) {
            throw new BadRequestException(
                    "This account is linked to a parent record. Unlink it before deleting the account.");
        }
        userRepository.delete(user);
    }

    // ------------------------------------------------------------------ helpers

    /**
     * Loads a user the caller is allowed to see and act on, or throws 403/404.
     *
     * <p>A super admin reaches every account in every school, which is what makes it
     * possible to reset a locked-out teacher's password or fix a mislinked parent without
     * routing the request through a school admin. A school admin keeps its original reach:
     * its own school, and never an admin account.
     */
    private User loadManageableUser(UUID id) {
        User target = userRepository.findWithSchoolById(id)
                .orElseThrow(() -> NotFoundException.of("User", id));
        User currentUser = access.currentUser();

        if (target.getId().equals(currentUser.getId())) {
            return target;
        }

        switch (currentUser.getRole()) {
            case SUPER_ADMIN -> {
                // Full override by design: every account, every school.
            }
            case SCHOOL_ADMIN -> {
                if (target.getRole() == UserRole.SUPER_ADMIN || target.getRole() == UserRole.SCHOOL_ADMIN) {
                    throw new AccessDeniedAppException("You cannot manage admin accounts");
                }
                Long own = currentUser.schoolIdOrNull();
                if (own == null || !own.equals(target.schoolIdOrNull())) {
                    throw new AccessDeniedAppException("You cannot manage users from another school");
                }
            }
            default -> throw new AccessDeniedAppException("You do not have permission to manage users");
        }
        return target;
    }

    private void assertRoleAssignable(User currentUser, UserRole newRole) {
        // A super admin may assign any role, including promoting another super admin.
        if (currentUser.getRole() == UserRole.SCHOOL_ADMIN
                && (newRole == UserRole.SUPER_ADMIN || newRole == UserRole.SCHOOL_ADMIN)) {
            throw new AccessDeniedAppException("A school admin cannot assign admin roles");
        }
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
