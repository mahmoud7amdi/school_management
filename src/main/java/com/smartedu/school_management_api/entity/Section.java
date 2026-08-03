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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * A named division of a grade — "A", "B", "Blue" — reusable across academic years.
 *
 * <p>Distinct from {@link Classroom}, which is the concrete year-bound group ("1-A"
 * in 2024/2025). A section is the stable label; a classroom is one instance of it.
 * Sections are optional: a small school can run a grade without dividing it.
 */
@Entity
@Table(name = "sections",
        uniqueConstraints = @UniqueConstraint(name = "uk_section_grade_name", columnNames = {"grade_id", "name"}),
        indexes = {
                @Index(name = "idx_section_school", columnList = "school_id"),
                @Index(name = "idx_section_grade", columnList = "grade_id")
        })
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Section extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Section name is required")
    @Size(max = 50, message = "Section name must not exceed 50 characters")
    @Column(nullable = false, length = 50)
    private String name;

    @Min(value = 1, message = "Capacity must be at least 1")
    @Max(value = 500, message = "Capacity must not exceed 500")
    @Column(name = "capacity")
    private Integer capacity;

    @Size(max = 255)
    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grade_id", nullable = false)
    private Grade grade;

    /** Teacher responsible for the section. Cleared, not cascaded, if they leave. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_head_id")
    private Teacher sectionHead;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    /** Classrooms keep their section label but are not deleted with it. */
    @OneToMany(mappedBy = "section", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Classroom> classrooms = new ArrayList<>();
}
