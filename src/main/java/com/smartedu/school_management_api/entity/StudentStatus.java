package com.smartedu.school_management_api.entity;

/** Lifecycle of a student's enrolment at a school. */
public enum StudentStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    GRADUATED("Graduated"),
    TRANSFERRED("Transferred"),
    SUSPENDED("Suspended");

    private final String label;

    StudentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
