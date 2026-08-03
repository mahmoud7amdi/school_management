package com.smartedu.school_management_api.entity;

/** How a student was marked for one school day. */
public enum AttendanceStatus {
    PRESENT("Present"),
    ABSENT("Absent"),
    LATE("Late"),
    EXCUSED("Excused"),
    HALF_DAY("Half Day");

    private final String label;

    AttendanceStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Fraction of a day credited as attended. Drives the attendance-rate figures
     * so a late arrival is not scored the same as a full absence.
     */
    public double weight() {
        return switch (this) {
            case PRESENT, EXCUSED -> 1.0;
            case LATE, HALF_DAY -> 0.5;
            case ABSENT -> 0.0;
        };
    }
}
