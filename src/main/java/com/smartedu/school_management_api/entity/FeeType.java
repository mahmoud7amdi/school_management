package com.smartedu.school_management_api.entity;

/** Category of a chargeable {@link FeeStructure} item. */
public enum FeeType {
    TUITION("Tuition"),
    TRANSPORT("Transport"),
    EXAM("Examination"),
    LIBRARY("Library"),
    LABORATORY("Laboratory"),
    HOSTEL("Hostel"),
    UNIFORM("Uniform"),
    ACTIVITY("Activity"),
    OTHER("Other");

    private final String label;

    FeeType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
