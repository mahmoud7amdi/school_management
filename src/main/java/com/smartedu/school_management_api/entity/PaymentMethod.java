package com.smartedu.school_management_api.entity;

/** How a {@link FeePayment} was tendered. */
public enum PaymentMethod {
    CASH("Cash"),
    CARD("Card"),
    BANK_TRANSFER("Bank Transfer"),
    MOBILE_MONEY("Mobile Money"),
    CHEQUE("Cheque"),
    WAIVER("Waiver");

    private final String label;

    PaymentMethod(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
