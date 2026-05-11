package com.smartedu.school_management_api.repository;
import com.smartedu.school_management_api.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface SchoolRepository extends JpaRepository<School, Long> {
    // التحقق من وجود المدرسة بالاسم لضمان عدم التكرار
    boolean existsByName(String name);
}
