package com.smartedu.school_management_api.repository;

import com.smartedu.school_management_api.entity.User;
import com.smartedu.school_management_api.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // إضافة دوال للبحث حسب الصلاحيات
    List<User> findByRoleNot(UserRole role);
    List<User> findByRole(UserRole role);
}
