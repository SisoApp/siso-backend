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
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Swagger/OpenAPI UI 허용
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/v3/api-docs.yaml",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

//                        // OAuth2 로그인 엔드포인트 허용
//                        .requestMatchers(
//                                "/oauth2/**",
//                                "/login/oauth2/**"
//                        ).permitAll()
//
//                        // 여기만 "로그인 필요"
//                        .requestMatchers(
//                                "/api/voice-samples/**",
//                                "/api/images/**"
//                        ).authenticated()



                        // 개발 단계: 모든 /api/** 경로 허용
                        .requestMatchers("/api/**").permitAll()
                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}