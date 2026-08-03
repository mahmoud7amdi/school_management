package com.smartedu.school_management_api.entity;

/** Clearing state of a single {@link FeePayment} row. */
public enum PaymentStatus {
    COMPLETED("Completed"),
    PENDING("Pending"),
    FAILED("Failed"),
    REFUNDED("Refunded");

    private final String label;

    PaymentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /** Only settled money reduces a student's outstanding balance. */
    public boolean countsTowardsBalance() {
        return this == COMPLETED;
    }
}
