package com.smartedu.school_management_api.entity;

/** Derived, never stored: how far a student has settled one fee item. */
public enum SettlementStatus {
    PAID("Paid"),
    PARTIAL("Partial"),
    UNPAID("Unpaid");

    private final String label;

    SettlementStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
