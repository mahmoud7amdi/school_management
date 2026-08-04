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

    /**
     * Roles allowed to manage academic data (years, grades, subjects, classrooms, students).
     *
     * <p>{@code SUPER_ADMIN} is deliberately excluded. Its remit is the platform — schools,
     * administrator appointments and reporting — not the day-to-day running of any one
     * school, which belongs to that school's own admin. Because every school-scoped
     * service routes through {@link
     * com.smartedu.school_management_api.service.SchoolAccessService}, and each of its
     * gates calls {@code requireAcademicManager}, excluding the role here is what keeps a
     * super admin out of school data even if a controller annotation is missed.
     */
    public boolean isAcademicManager() {
        return this == SCHOOL_ADMIN;
    }

    /** Roles that administer the platform itself: schools, admin accounts, reports. */
    public boolean isPlatformAdmin() {
        return this == SUPER_ADMIN;
    }

    /** Roles holding an administrative account, as opposed to a portal account. */
    public boolean isAdmin() {
        return this == SUPER_ADMIN || this == SCHOOL_ADMIN;
    }
}
