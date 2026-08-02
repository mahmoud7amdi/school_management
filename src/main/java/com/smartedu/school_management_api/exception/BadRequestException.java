package com.smartedu.school_management_api.exception;

/** Request is well-formed but semantically invalid. Mapped to 400. */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
