package com.smartedu.school_management_api.entity;

/** Kind of assessment an {@link Exam} represents. */
public enum ExamType {
    QUIZ("Quiz"),
    ASSIGNMENT("Assignment"),
    MIDTERM("Midterm"),
    FINAL("Final"),
    PRACTICAL("Practical"),
    MOCK("Mock");

    private final String label;

    ExamType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
