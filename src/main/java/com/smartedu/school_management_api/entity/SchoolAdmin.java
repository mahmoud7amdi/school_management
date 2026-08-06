package com.smartedu.school_management_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * An administrative staff member of a school.
 *
 * <p>Completes the set alongside {@link Teacher}, {@link Student} and {@link Parent}: the
 * personnel record exists in its own right, and {@code userAccount} is the optional bridge
 * to a login. Keeping it separate from {@link User} means an administrator's appointment,
 * office and job title survive their account being disabled, and the same record can be
 * re-linked if access is later restored.
 *
 * <p>Distinct from {@link UserRole#SCHOOL_ADMIN}, which answers "what may this login do";
 * this entity answers "who is this person and what is their post".
 */
@Entity
@Table(name = "school_admins",
        uniqueConstraints = @UniqueConstraint(name = "uk_school_admin_school_email",
                columnNames = {"school_id", "email"}),
        indexes = {
                @Index(name = "idx_school_admin_school", columnList = "school_id")
        })
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SchoolAdmin extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "First name is required")
    @Size(max = 60, message = "First name must not exceed 60 characters")
    @Column(name = "first_name", nullable = false, length = 60)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 60, message = "Last name must not exceed 60 characters")
    @Column(name = "last_name", nullable = false, length = 60)
    private String lastName;

    /**
     * Optional, but unique within the school when present. MySQL treats NULLs as
     * distinct in a unique index, so several records may have no email on file.
     */
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

    // --- Appointment ------------------------------------------------------
    /** Post held, e.g. "Registrar" or "Deputy Head". */
    @NotBlank(message = "Job title is required")
    @Size(max = 120, message = "Job title must not exceed 120 characters")
    @Column(name = "job_title", nullable = false, length = 120)
    private String jobTitle;

    @Size(max = 120, message = "Department must not exceed 120 characters")
    @Column(name = "department", length = 120)
    private String department;

    @Size(max = 60, message = "Office must not exceed 60 characters")
    @Column(name = "office", length = 60)
    private String office;

    @NotNull(message = "Appointment date is required")
    @Column(name = "appointment_date", nullable = false)
    private LocalDate appointmentDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    /** Optional login for this administrator. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_account_id", unique = true)
    private User userAccount;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
