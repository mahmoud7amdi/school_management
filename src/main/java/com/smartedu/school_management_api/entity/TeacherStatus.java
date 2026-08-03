package com.smartedu.school_management_api.entity;

/** Employment state of a {@link Teacher}. */
public enum TeacherStatus {
    ACTIVE("Active"),
    ON_LEAVE("On Leave"),
    SUSPENDED("Suspended"),
    RESIGNED("Resigned"),
    RETIRED("Retired");

    private final String label;

    TeacherStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** Whether a teacher in this state may still be assigned to classes. */
    public boolean isAssignable() {
        return this == ACTIVE || this == ON_LEAVE;
    }
}
