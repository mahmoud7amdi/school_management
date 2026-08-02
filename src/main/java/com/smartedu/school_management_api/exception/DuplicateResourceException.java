package com.smartedu.school_management_api.exception;

/** A uniqueness rule was violated. Mapped to 409 by the global handler. */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
