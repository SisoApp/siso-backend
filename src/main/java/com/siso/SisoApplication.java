package com.siso;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing // JPA Auditing 활성화 - createdAt, updatedAt 자동 설정
@ConfigurationPropertiesScan // @ConfigurationProperties 자동 스캔
public class SisoApplication {
	public static void main(String[] args) {
		SpringApplication.run(SisoApplication.class, args);
	}

}
