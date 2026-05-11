package com.smartedu.school_management_api.service.impl;

import com.smartedu.school_management_api.dto.LoginResponse;
import com.smartedu.school_management_api.entity.TokenBlacklist;
import com.smartedu.school_management_api.entity.User;
import com.smartedu.school_management_api.entity.UserRole;
import com.smartedu.school_management_api.repository.TokenBlacklistRepository;
import com.smartedu.school_management_api.repository.UserRepository;
import com.smartedu.school_management_api.service.UserService;
import com.smartedu.school_management_api.utils.JwtUtil;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder; // تم حقنه من SecurityConfig
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistRepository tokenBlacklistRepository;

    @PostConstruct
    public void initAdmin() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123")) // يمكن تغييره
                    .email("admin@smartedu.com")
                    .role(UserRole.SUPER_ADMIN)
                    .active(true)
                    .build();
            userRepository.save(admin);
        }
    }

    @Override
    public User createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username is already taken");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email is already taken");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        if (currentUser.getRole() == UserRole.SUPER_ADMIN) {
            if (user.getRole() != UserRole.SCHOOL_ADMIN) {
                throw new RuntimeException("Super Admin can only create users with SCHOOL_ADMIN role");
            }
        } else if (currentUser.getRole() == UserRole.SCHOOL_ADMIN) {
            if (user.getRole() == UserRole.SUPER_ADMIN || user.getRole() == UserRole.SCHOOL_ADMIN) {
                throw new RuntimeException("School Admin cannot create SUPER_ADMIN or SCHOOL_ADMIN users");
            }
        } else {
             throw new RuntimeException("You do not have permission to create users");
        }

        // تشفير كلمة المرور قبل الحفظ
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        if (currentUser.getRole() == UserRole.SUPER_ADMIN) {
            // Super Admin only sees SCHOOL_ADMIN users
            return userRepository.findByRole(UserRole.SCHOOL_ADMIN);
        } else if (currentUser.getRole() == UserRole.SCHOOL_ADMIN) {
            // School Admin sees all users except SUPER_ADMIN
            return userRepository.findByRoleNot(UserRole.SUPER_ADMIN);
        } else {
            throw new RuntimeException("You do not have permission to view users");
        }
    }

    @Override
    public User getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        if (currentUser.getRole() == UserRole.SUPER_ADMIN) {
            if (user.getRole() != UserRole.SCHOOL_ADMIN) {
                throw new RuntimeException("You do not have permission to view this user");
            }
        } else if (currentUser.getRole() == UserRole.SCHOOL_ADMIN) {
            if (user.getRole() == UserRole.SUPER_ADMIN) {
                 throw new RuntimeException("You do not have permission to view this user");
            }
        }

        return user;
    }
    
    @Override
    public User getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
        
        // Users can always get their own profile
        if (currentUser.getUsername().equals(username)) {
            return user;
        }

        if (currentUser.getRole() == UserRole.SUPER_ADMIN) {
            if (user.getRole() != UserRole.SCHOOL_ADMIN) {
                throw new RuntimeException("You do not have permission to view this user");
            }
        } else if (currentUser.getRole() == UserRole.SCHOOL_ADMIN) {
            if (user.getRole() == UserRole.SUPER_ADMIN) {
                 throw new RuntimeException("You do not have permission to view this user");
            }
        } else {
             throw new RuntimeException("You do not have permission to view this user");
        }

        return user;
    }

    @Override
    @Transactional
    public User updateUser(Long id, User userDetails) {
        User user = getUserById(id); // Reuse the get logic which contains the role check

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        if (currentUser.getRole() == UserRole.SCHOOL_ADMIN && user.getRole() == UserRole.SUPER_ADMIN) {
             throw new RuntimeException("School Admin cannot update SUPER_ADMIN users");
        }
        
        if (currentUser.getRole() == UserRole.SUPER_ADMIN && user.getRole() != UserRole.SCHOOL_ADMIN) {
             throw new RuntimeException("Super Admin can only update SCHOOL_ADMIN users");
        }

        if (userDetails.getUsername() != null && !userDetails.getUsername().isBlank()) {
             // تحقق مما إذا كان اليوزرنيم الجديد مأخوذ بالفعل من قبل مستخدم آخر
             if (!user.getUsername().equals(userDetails.getUsername()) && userRepository.existsByUsername(userDetails.getUsername())) {
                 throw new RuntimeException("Username is already taken");
             }
             user.setUsername(userDetails.getUsername());
        }

        if (userDetails.getEmail() != null && !userDetails.getEmail().isBlank()) {
             // تحقق مما إذا كان الإيميل الجديد مأخوذ بالفعل من قبل مستخدم آخر
             if (!user.getEmail().equals(userDetails.getEmail()) && userRepository.existsByEmail(userDetails.getEmail())) {
                 throw new RuntimeException("Email is already taken");
             }
            user.setEmail(userDetails.getEmail());
        }
        
        // إذا قام المستخدم بتغيير الباسورد، نقوم بتشفيره مجدداً
        if (userDetails.getPassword() != null && !userDetails.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }
        
        if (userDetails.getRole() != null) {
            // Check if the current user has permission to change to the new role
             if (currentUser.getRole() == UserRole.SUPER_ADMIN && userDetails.getRole() != UserRole.SCHOOL_ADMIN) {
                throw new RuntimeException("Super Admin can only set role to SCHOOL_ADMIN");
             } else if (currentUser.getRole() == UserRole.SCHOOL_ADMIN && (userDetails.getRole() == UserRole.SUPER_ADMIN || userDetails.getRole() == UserRole.SCHOOL_ADMIN)) {
                throw new RuntimeException("School Admin cannot set role to SUPER_ADMIN or SCHOOL_ADMIN");
             }
            user.setRole(userDetails.getRole());
        }
        
        if (userDetails.getActive() != null) {
            user.setActive(userDetails.getActive());
        }

        if (userDetails.getSchool() != null) {
            user.setSchool(userDetails.getSchool());
        }

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id); // Use getUserById to enforce read permissions

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        if (currentUser.getRole() == UserRole.SCHOOL_ADMIN && user.getRole() == UserRole.SUPER_ADMIN) {
             throw new RuntimeException("School Admin cannot delete SUPER_ADMIN users");
        }
        
        if (currentUser.getRole() == UserRole.SUPER_ADMIN && user.getRole() != UserRole.SCHOOL_ADMIN) {
             throw new RuntimeException("Super Admin can only delete SCHOOL_ADMIN users");
        }

        userRepository.deleteById(id);
    }

    // دالة تسجيل الدخول (تقوم بإرجاع التوكن وبيانات المستخدم)
    public LoginResponse login(String username, String password) {
        // التحقق من اسم المستخدم وكلمة المرور
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

        // جلب بيانات المستخدم لإنشاء التوكن
        final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        final String token = jwtUtil.generateToken(userDetails);

        // جلب بيانات المستخدم الكاملة من قاعدة البيانات
        User user = userRepository.findByUsername(username).orElseThrow();

        return new LoginResponse(token, user);
    }

    // دالة تسجيل الخروج لإلغاء التوكن وإضافته للقائمة السوداء
    @Override
    public void logout(HttpServletRequest request) {
        final String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwt = authorizationHeader.substring(7);

            // إذا كان التوكن موجوداً بالفعل في القائمة السوداء لا تفعل شيئاً
            if (!tokenBlacklistRepository.existsByToken(jwt)) {
                try {
                    // استخراج تاريخ انتهاء التوكن لتخزينه في الداتابيز
                    Date expirationDate = jwtUtil.extractExpiration(jwt);

                    // حفظ التوكن في القائمة السوداء
                    TokenBlacklist blacklist = TokenBlacklist.builder()
                            .token(jwt)
                            .expiryDate(expirationDate)
                            .build();

                    tokenBlacklistRepository.save(blacklist);
                } catch (Exception e) {
                    throw new RuntimeException("Invalid or expired token");
                }
            }
        } else {
            throw new RuntimeException("No valid token provided for logout");
        }
    }
}
