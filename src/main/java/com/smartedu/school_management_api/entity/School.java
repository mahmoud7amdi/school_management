package com.smartedu.school_management_api.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "schools")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "اسم المدرسة مطلوب")
    @Column(nullable = false, unique = true)
    private String name;

    @Column(name = "address")
    private String address;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Email(message = "يرجى إدخال بريد إلكتروني صحيح")
    @Column(unique = true)
    private String email;

    @Column(name = "logo_url")
    private String logoUrl; // لتخزين رابط شعار المدرسة

    @Column(name = "website")
    private String website;

    // علاقة المدرسة بالفصول (مدرسة واحدة لها فصول كثيرة)
    // قمنا بوضع تعليق هنا لأننا سننشئ كلاس Classroom لاحقاً
    // @OneToMany(mappedBy = "school", cascade = CascadeType.ALL)
    // private List<Classroom> classrooms;

    // توقيت الإنشاء والتحديث تلقائياً (احترافي جداً)
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", updatable = false)
    private java.util.Date createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = new java.util.Date();
    }
}
