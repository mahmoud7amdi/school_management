package com.smartedu.school_management_api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "اسم المستخدم مطلوب")
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank(message = "كلمة المرور مطلوبة")
    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) // يخفي الباسورد عند إرجاع الكائن في JSON
    private String password;

    @Email(message = "البريد الإلكتروني غير صحيح")
    @Column(unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(columnDefinition = "boolean default true")
    private Boolean active = true;

    // ربط المستخدم بالمدرسة
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id")
    private School school;
}
