package com.smartedu.school_management_api.entity;

/** Roles a {@link User} can hold. Order is significant: lower ordinal = wider scope. */
public enum UserRole {
    SUPER_ADMIN("Super Admin"),
    SCHOOL_ADMIN("School Admin"),
    TEACHER("Teacher"),
    STUDENT("Student"),
    PARENT("Parent");

    private final String label;

    UserRole(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** Roles allowed to manage academic data (years, grades, subjects, classrooms, students). */
    public boolean isAcademicManager() {
        return this == SUPER_ADMIN || this == SCHOOL_ADMIN;
    }
}
