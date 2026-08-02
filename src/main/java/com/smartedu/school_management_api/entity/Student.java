package com.smartedu.school_management_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.Period;

/**
 * An enrolled student.
 *
 * <p>Kept separate from {@link User} on purpose: a student is a school record that
 * exists whether or not anyone ever signs in as them. {@code userAccount} is the
 * optional bridge to a login, so registrars can enrol first and grant access later.
 * The admission number is the human-facing key and is unique per school.
 */
@Entity
@Table(name = "students",
        uniqueConstraints = @UniqueConstraint(name = "uk_student_school_admission",
                columnNames = {"school_id", "admission_number"}),
        indexes = {
                @Index(name = "idx_student_school", columnList = "school_id"),
                @Index(name = "idx_student_classroom", columnList = "classroom_id"),
                @Index(name = "idx_student_grade", columnList = "grade_id"),
                @Index(name = "idx_student_status", columnList = "status")
        })
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Student extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Registration number as printed on school documents, unique within the school. */
    @NotBlank(message = "Admission number is required")
    @Size(max = 40, message = "Admission number must not exceed 40 characters")
    @Column(name = "admission_number", nullable = false, length = 40)
    private String admissionNumber;

    @NotBlank(message = "First name is required")
    @Size(max = 60, message = "First name must not exceed 60 characters")
    @Column(name = "first_name", nullable = false, length = 60)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 60, message = "Last name must not exceed 60 characters")
    @Column(name = "last_name", nullable = false, length = 60)
    private String lastName;

    @NotNull(message = "Gender is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Email(message = "Invalid email")
    @Size(max = 150)
    @Column(length = 150)
    private String email;

    @Size(max = 30)
    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Size(max = 255)
    @Column(name = "address")
    private String address;

    @Size(max = 500)
    @Column(name = "photo_url", length = 500)
    private String photoUrl;

    // --- Guardian ---------------------------------------------------------
    @Size(max = 120)
    @Column(name = "guardian_name", length = 120)
    private String guardianName;

    @Size(max = 30)
    @Column(name = "guardian_phone", length = 30)
    private String guardianPhone;

    @Email(message = "Invalid guardian email")
    @Size(max = 150)
    @Column(name = "guardian_email", length = 150)
    private String guardianEmail;

    // --- Enrolment --------------------------------------------------------
    @NotNull(message = "Enrollment date is required")
    @Column(name = "enrollment_date", nullable = false)
    private LocalDate enrollmentDate;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StudentStatus status = StudentStatus.ACTIVE;

    /** Grade level. Required — a student always sits at some level. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grade_id", nullable = false)
    private Grade grade;

    /** Class group. Optional, so a student can be enrolled before placement. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id")
    private Classroom classroom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    /** Optional login for this student. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_account_id", unique = true)
    private User userAccount;

    public String getFullName() {
        return firstName + " " + lastName;
    }

    /** Age in whole years, derived rather than stored so it can't drift. */
    public Integer getAge() {
        return dateOfBirth != null ? Period.between(dateOfBirth, LocalDate.now()).getYears() : null;
    }
}
