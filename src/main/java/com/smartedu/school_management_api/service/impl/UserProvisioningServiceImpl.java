package com.smartedu.school_management_api.service.impl;

import com.smartedu.school_management_api.dto.user.AccountCredentials;
import com.smartedu.school_management_api.entity.School;
import com.smartedu.school_management_api.entity.User;
import com.smartedu.school_management_api.entity.UserRole;
import com.smartedu.school_management_api.exception.BadRequestException;
import com.smartedu.school_management_api.exception.DuplicateResourceException;
import com.smartedu.school_management_api.repository.UserRepository;
import com.smartedu.school_management_api.service.UserProvisioningService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * The account half of adding a person.
 *
 * <p>Deliberately narrow: it validates the credentials, encodes the password and saves one
 * {@link User}. It does not decide which school the account belongs to, nor which role it
 * gets — both arrive from the caller, which is what keeps a client from steering either.
 */
@Service
@RequiredArgsConstructor
public class UserProvisioningServiceImpl implements UserProvisioningService {

    private static final Logger log = LoggerFactory.getLogger(UserProvisioningServiceImpl.class);

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    /**
     * Joins the caller's transaction rather than starting its own — mandatory, in fact, so
     * this can never be called outside one and quietly commit an account whose person record
     * later fails to save.
     */
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public User provisionAccount(AccountCredentials credentials,
                                 UserRole role,
                                 String fullName,
                                 String email,
                                 String phoneNumber,
                                 School school) {
        if (credentials == null) {
            throw new BadRequestException("Sign-in details are required to create the account");
        }

        String username = trim(credentials.username());
        String normalizedEmail = lower(email);

        if (normalizedEmail == null || normalizedEmail.isEmpty()) {
            // The account cannot exist without one: users.email is NOT NULL and unique.
            throw new BadRequestException("An email address is required to create the sign-in account");
        }
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new DuplicateResourceException("Username '" + username + "' is already taken");
        }
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new DuplicateResourceException("Email '" + normalizedEmail + "' is already registered");
        }

        User account = userRepository.save(User.builder()
                .username(username)
                .fullName(trim(fullName))
                .email(normalizedEmail)
                .password(passwordEncoder.encode(credentials.password()))
                .role(role)
                .phoneNumber(trimToNull(phoneNumber))
                .active(credentials.activeOrDefault())
                .school(school)
                .build());

        log.info("Provisioned {} account '{}' in school {}",
                role, account.getUsername(), school == null ? "none" : school.getId());

        return account;
    }

    // --------------------------------------------------------------- helpers

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    private static String trimToNull(String value) {
        String trimmed = trim(value);
        return trimmed == null || trimmed.isEmpty() ? null : trimmed;
    }

    private static String lower(String value) {
        String trimmed = trim(value);
        return trimmed == null ? null : trimmed.toLowerCase();
    }
}
