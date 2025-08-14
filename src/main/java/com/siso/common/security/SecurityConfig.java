package com.siso.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/voice-samples/**").permitAll() // 로그인일때 가능 - 테스트용으로 임시 허용
                        .requestMatchers("/api/images/**").permitAll() // 테스트용으로 이미지 API 허용
                        .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.disable()); // API 테스트용 CSRF 비활성화

        return http.build();
    }
}
