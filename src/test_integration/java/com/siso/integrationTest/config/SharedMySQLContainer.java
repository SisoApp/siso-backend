package com.siso.integrationTest.config;

import org.testcontainers.containers.MySQLContainer;

/**
 * 모든 통합 테스트에서 공유하는 MySQL 컨테이너
 *
 * Singleton 패턴으로 컨테이너를 한 번만 생성합니다.
 */
public class SharedMySQLContainer {

    private static final MySQLContainer<?> MYSQL;

    static {
        MYSQL = new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("siso_test")
                .withUsername("test")
                .withPassword("test");
        MYSQL.start();
    }

    public static MySQLContainer<?> getInstance() {
        return MYSQL;
    }
}
