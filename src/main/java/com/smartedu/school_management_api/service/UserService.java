package com.smartedu.school_management_api.service;

import com.smartedu.school_management_api.dto.LoginResponse;
import com.smartedu.school_management_api.entity.User;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

public interface UserService {

    // 1. إنشاء مستخدم جديد بواسطة الإدمن (مع تشفير الباسورد)
    User createUser(User user);

    // 2. تسجيل الدخول (للمصادقة الأولية)
    LoginResponse login(String username, String password);

    // 3. تسجيل الخروج وإلغاء التوكن
    void logout(HttpServletRequest request);

    // 4. جلب جميع المستخدمين
    List<User> getAllUsers();

    // 5. جلب مستخدم معين بواسطة ID
    User getUserById(Long id);

    // 6. تحديث بيانات المستخدم
    User updateUser(Long id, User userDetails);

    // 7. حذف مستخدم من النظام
    void deleteUser(Long id);

    // 8. البحث عن مستخدم بواسطة اسم المستخدم (مفيدة للـ Security لاحقاً)
    User getUserByUsername(String username);
}
