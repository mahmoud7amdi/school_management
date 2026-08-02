package com.smartedu.school_management_api.entity;

import jakarta.persistence.CascadeType;
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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/** A grade level ("Grade 1", "Year 7"), unique per school. */
@Entity
@Table(name = "grades",
        uniqueConstraints = @UniqueConstraint(name = "uk_grade_school_name", columnNames = {"school_id", "name"}),
        indexes = @Index(name = "idx_grade_school", columnList = "school_id"))
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Grade extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Grade name is required")
    @Size(max = 80, message = "Grade name must not exceed 80 characters")
    @Column(nullable = false, length = 80)
    private String name;

    /** Sort order in listings; lower comes first. */
    @Column(name = "level_order")
    private Integer levelOrder;

    @Size(max = 255)
    @Column(name = "description")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    @OneToMany(mappedBy = "grade", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<Subject> subjects = new ArrayList<>();

    @OneToMany(mappedBy = "grade", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<Classroom> classrooms = new ArrayList<>();
}
