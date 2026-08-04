package com.smartedu.school_management_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Content of the public About Us page.
 *
 * <p>A single row, edited by a super admin. In the database rather than the template so
 * the wording can change without a redeploy, which is the point of making it editable at
 * all — and platform content rather than school content, so it belongs to the super
 * admin's remit alongside schools and administrator accounts.
 *
 * <p>The row is created on first save. Until then the service serves built-in defaults,
 * so the page is never blank.
 */
@Entity
@Table(name = "about_page")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AboutPage extends BaseEntity {

    /**
     * Always 1. The table holds one row, and pinning the id makes that explicit:
     * a save can never quietly create a second version of the page.
     */
    public static final long SINGLETON_ID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title must not exceed 150 characters")
    @Column(nullable = false, length = 150)
    private String title;

    @Size(max = 300, message = "Tagline must not exceed 300 characters")
    @Column(length = 300)
    private String tagline;

    /** Main copy. {@code @Lob} because this is prose, not a label. */
    @Lob
    @Column(columnDefinition = "TEXT")
    private String body;

    @Lob
    @Column(name = "mission", columnDefinition = "TEXT")
    private String mission;

    @Email(message = "Please enter a valid email")
    @Size(max = 150)
    @Column(name = "contact_email", length = 150)
    private String contactEmail;

    @Size(max = 30, message = "Phone number must not exceed 30 characters")
    @Column(name = "contact_phone", length = 30)
    private String contactPhone;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    @Column(length = 255)
    private String address;

    @Size(max = 255)
    @Column(length = 255)
    private String website;

    /** Who last edited it, for the "last updated" line on the editor. */
    @Size(max = 120)
    @Column(name = "updated_by", length = 120)
    private String updatedBy;
}
