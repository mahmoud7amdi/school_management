package com.smartedu.school_management_api.exception;

/** Requested resource does not exist. Mapped to 404 by the global handler. */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException of(String resource, Object id) {
        return new NotFoundException(resource + " not found with ID: " + id);
    }
}
