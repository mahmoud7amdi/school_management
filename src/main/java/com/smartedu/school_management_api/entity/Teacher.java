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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A member of teaching staff.
 *
 * <p>Mirrors {@link Student}: the employment record exists independently of any
 * login, and {@code userAccount} is the optional bridge to one. That lets a
 * registrar capture staff details before IT provisions access, and lets a teacher
 * who leaves keep their historical record after the account is disabled.
 *
 * <p>Note {@link Classroom#getClassTeacher()} still points at {@link User}: it
 * answers "who signs in to manage this class", which is an access question rather
 * than an HR one.
 */
@Entity
@Table(name = "teachers",
        uniqueConstraints = @UniqueConstraint(name = "uk_teacher_school_employee",
                columnNames = {"school_id", "employee_number"}),
        indexes = {
                @Index(name = "idx_teacher_school", columnList = "school_id"),
                @Index(name = "idx_teacher_status", columnList = "status")
        })
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Teacher extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Staff number as used on payroll, unique within the school. */
    @NotBlank(message = "Employee number is required")
    @Size(max = 40, message = "Employee number must not exceed 40 characters")
    @Column(name = "employee_number", nullable = false, length = 40)
    private String employeeNumber;

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

    @Past(message = "Date of birth must be in the past")
    @Column(name = "date_of_birth")
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

    // --- Employment -------------------------------------------------------
    @Size(max = 150, message = "Qualification must not exceed 150 characters")
    @Column(name = "qualification", length = 150)
    private String qualification;

    @Size(max = 150, message = "Specialization must not exceed 150 characters")
    @Column(name = "specialization", length = 150)
    private String specialization;

    @NotNull(message = "Hire date is required")
    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TeacherStatus status = TeacherStatus.ACTIVE;

    /**
     * Subjects this teacher is qualified to teach. Owning side of the join table;
     * removing a teacher clears the rows without touching the subjects themselves.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "teacher_subjects",
            joinColumns = @JoinColumn(name = "teacher_id"),
            inverseJoinColumns = @JoinColumn(name = "subject_id"),
            uniqueConstraints = @UniqueConstraint(name = "uk_teacher_subject",
                    columnNames = {"teacher_id", "subject_id"}))
    @Builder.Default
    private Set<Subject> subjects = new LinkedHashSet<>();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    /** Optional login for this teacher. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_account_id", unique = true)
    private User userAccount;

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
