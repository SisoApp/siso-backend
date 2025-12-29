package com.siso.config;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * 통합 테스트 기본 클래스
 *
 * 이 클래스를 상속받아 통합 테스트를 작성하세요.
 * Testcontainers를 사용하여 실제 MySQL 컨테이너로 테스트합니다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional  // 각 테스트 후 롤백
public abstract class IntegrationTestBase {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("siso_test")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    protected MockMvc mockMvc;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        // Testcontainers MySQL 설정
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);

        // 테스트용 설정
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");

        // AWS S3 비활성화 (Mock 사용)
        registry.add("cloud.aws.stack.auto", () -> "false");
        registry.add("cloud.aws.region.static", () -> "ap-northeast-2");

        // FCM 비활성화 (Mock 사용)
        registry.add("fcm.enabled", () -> "false");
    }

    @BeforeEach
    void setUp() {
        // 각 테스트 전에 공통 설정
    }
}
