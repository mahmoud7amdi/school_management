package com.smartedu.school_management_api.entity;

/** How a {@link Parent} is related to a {@link Student}. */
public enum GuardianRelationship {
    MOTHER("Mother"),
    FATHER("Father"),
    GUARDIAN("Guardian"),
    OTHER("Other");

    private final String label;

    GuardianRelationship(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
