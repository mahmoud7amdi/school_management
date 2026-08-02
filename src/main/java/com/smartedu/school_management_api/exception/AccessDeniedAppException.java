package com.smartedu.school_management_api.exception;

/**
 * The caller is authenticated but not allowed to touch this resource — typically
 * a cross-tenant attempt. Mapped to 403 by the global handler.
 */
public class AccessDeniedAppException extends RuntimeException {

    public AccessDeniedAppException(String message) {
        super(message);
    }
}
