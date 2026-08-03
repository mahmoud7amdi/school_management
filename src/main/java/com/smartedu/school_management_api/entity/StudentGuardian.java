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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * The link between a {@link Parent} and a {@link Student}.
 *
 * <p>Modelled as an entity rather than a plain {@code @ManyToMany} because the link
 * carries its own data — the same reasoning as {@link Enrollment}. Many-to-many in both
 * directions is deliberate: two guardians can share one child, and one guardian can have
 * several children in the school.
 *
 * <p>Unique on (parent, student) so the same pair cannot be linked twice.
 */
@Entity
@Table(name = "parent_students",
        uniqueConstraints = @UniqueConstraint(name = "uk_parent_student",
                columnNames = {"parent_id", "student_id"}),
        indexes = {
                @Index(name = "idx_parent_student_parent", columnList = "parent_id"),
                @Index(name = "idx_parent_student_student", columnList = "student_id")
        })
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudentGuardian extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Relationship is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GuardianRelationship relationship;

    /**
     * The guardian the school contacts first. Not enforced as "exactly one per student"
     * at the database level; the service clears the flag on siblings when one is set.
     */
    @Column(name = "primary_contact", nullable = false)
    @Builder.Default
    private Boolean primaryContact = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "parent_id", nullable = false)
    private Parent parent;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
}
