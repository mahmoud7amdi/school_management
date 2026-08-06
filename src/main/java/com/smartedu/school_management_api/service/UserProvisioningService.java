package com.smartedu.school_management_api.service;

import com.smartedu.school_management_api.dto.user.AccountCredentials;
import com.smartedu.school_management_api.entity.School;
import com.smartedu.school_management_api.entity.User;
import com.smartedu.school_management_api.entity.UserRole;

/**
 * Creates the login that belongs to a teacher, student or parent record.
 *
 * <p>Adding a person and creating their account used to be two separate jobs: the JSON API's
 * {@code UserService.createUser} made an account and nothing else, and whoever added the
 * Teacher/Student/Parent record afterwards had to remember to link the two by hand. This
 * service removes that step — the three domain services call it while building the record, so
 * the account is provisioned in the background as part of the same operation.
 *
 * <p>Callers are already inside their own {@code @Transactional} create method, so the
 * account and the record commit or roll back together: a duplicate employee number
 * discovered after the account was saved leaves no orphan login behind.
 */
public interface UserProvisioningService {

    /**
     * Saves and returns the account for a person being created.
     *
     * <p>{@code school} is passed in by the caller, which has already resolved it from the
     * signed-in admin's own tenant — it is never taken from a request body, so an account
     * cannot be steered into another school.
     *
     * @param credentials username and initial password from the form
     * @param role        the account's role, fixed by the caller rather than chosen by the client
     * @param fullName    display name, assembled from the person's first and last name
     * @param email       the person's email; also the account's, and unique across all accounts
     * @param phoneNumber optional contact number, may be null
     * @param school      tenant the account belongs to
     * @throws com.smartedu.school_management_api.exception.DuplicateResourceException
     *         if the username or email is already in use
     */
    User provisionAccount(AccountCredentials credentials,
                          UserRole role,
                          String fullName,
                          String email,
                          String phoneNumber,
                          School school);
}
