package com.smartedu.school_management_api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Verifies the Spring context starts: every bean resolves, the JPA mappings are
 * valid, and the security chains build. Runs against in-memory H2 so it needs no
 * MySQL instance (see application-test.yaml).
 */
@SpringBootTest
@ActiveProfiles("test")
class SchoolManagementApiApplicationTests {

	@Test
	void contextLoads() {
	}

}
