package com.siso.integrationTest.config;

import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.services.s3.S3Client;

@TestConfiguration
public class TestAwsConfig {
    @Bean
    public S3Client s3Client() {
        return Mockito.mock(S3Client.class);
    }
}

