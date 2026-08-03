package com.smartedu.school_management_api.entity;

/** Outcome of a student's enrolment for one academic year. */
public enum EnrollmentStatus {
    ENROLLED("Enrolled"),
    COMPLETED("Completed"),
    REPEATING("Repeating"),
    WITHDRAWN("Withdrawn"),
    TRANSFERRED("Transferred");

    private final String label;

    EnrollmentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** Only an open enrolment counts towards a classroom's occupancy. */
    public boolean isOpen() {
        return this == ENROLLED || this == REPEATING;
    }
}
