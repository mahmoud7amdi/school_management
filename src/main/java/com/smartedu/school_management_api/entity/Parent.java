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
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A parent or guardian contact.
 *
 * <p>Mirrors {@link Teacher} and {@link Student}: the contact record exists whether or
 * not anyone ever signs in as them, and {@code userAccount} is the optional bridge to a
 * login. That lets a registrar capture guardian details during enrolment and grant portal
 * access later, and lets the record outlive a disabled account.
 *
 * <p>Children are linked through {@link StudentGuardian} rather than a direct collection,
 * because the link itself carries the relationship and the primary-contact flag.
 *
 * <p>The free-text {@code guardianName}/{@code guardianPhone}/{@code guardianEmail} fields
 * on {@link Student} are unaffected — they remain the quick "who do we call" note, while
 * this entity is the one that can hold a login.
 */
@Entity
@Table(name = "parents",
        uniqueConstraints = @UniqueConstraint(name = "uk_parent_school_email",
                columnNames = {"school_id", "email"}),
        indexes = {
                @Index(name = "idx_parent_school", columnList = "school_id")
        })
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Parent extends BaseEntity {

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
     * distinct in a unique index, so several guardians may have no email on file.
     */
    @Email(message = "Invalid email")
    @Size(max = 150)
    @Column(length = 150)
    private String email;

    @Size(max = 30)
    @Column(name = "phone_number", length = 30)
    private String phoneNumber;

    @Size(max = 120, message = "Occupation must not exceed 120 characters")
    @Column(name = "occupation", length = 120)
    private String occupation;

    @Size(max = 255)
    @Column(name = "address")
    private String address;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    /** Optional login for this guardian. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_account_id", unique = true)
    private User userAccount;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
