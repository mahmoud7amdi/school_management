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

/** A subject taught within a grade, unique per grade. */
@Entity
@Table(name = "subjects",
        uniqueConstraints = @UniqueConstraint(name = "uk_subject_grade_name", columnNames = {"grade_id", "name"}),
        indexes = {
                @Index(name = "idx_subject_school", columnList = "school_id"),
                @Index(name = "idx_subject_grade", columnList = "grade_id")
        })
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Subject extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Subject name is required")
    @Size(max = 100, message = "Subject name must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @Size(max = 20)
    @Column(name = "code", length = 20)
    private String code;

    @Min(value = 1, message = "Weekly hours must be at least 1")
    @Max(value = 60, message = "Weekly hours must not exceed 60")
    @Column(name = "weekly_hours")
    private Integer weeklyHours;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grade_id", nullable = false)
    private Grade grade;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;
}
