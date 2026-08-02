package com.smartedu.school_management_api.config;

import com.smartedu.school_management_api.service.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

/**
 * Seeds the bootstrap super admin once the context is up.
 *
 * <p>An {@code ApplicationRunner} rather than {@code @PostConstruct}: the latter fires
 * before the transaction infrastructure is ready, so the insert ran outside a
 * transaction.
 */
@Component
@RequiredArgsConstructor
public class AdminBootstrapRunner implements ApplicationRunner {

    private final UserServiceImpl userService;

    @Override
    public void run(ApplicationArguments args) {
        userService.initAdmin();
    }
}
