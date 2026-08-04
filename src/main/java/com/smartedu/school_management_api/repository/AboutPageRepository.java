package com.smartedu.school_management_api.repository;

import com.smartedu.school_management_api.entity.AboutPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * The About page row. Read through {@code findById(SINGLETON_ID)}: there is only ever
 * one, so no finders beyond the inherited ones are needed.
 */
@Repository
public interface AboutPageRepository extends JpaRepository<AboutPage, Long> {
}
