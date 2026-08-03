package com.smartedu.school_management_api.entity;

/** Review state of an {@link AbsenceNote} submitted by a student or parent. */
public enum AbsenceNoteStatus {
    SUBMITTED("Submitted"),
    ACKNOWLEDGED("Acknowledged"),
    REJECTED("Rejected");

    private final String label;

    AbsenceNoteStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
