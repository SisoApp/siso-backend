package com.siso.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siso.user.infrastructure.jwt.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final JwtRequestFilter jwtRequestFilter;
    private final RefreshTokenAuthenticationProvider refreshTokenAuthenticationProvider;
    private final JwtTokenUtil jwtTokenUtil;
    private final ObjectMapper objectMapper;
    private final TokenService tokenService;

    // 1) 보안체인 자체를 타지 않게 완전 제외할 경로 (정적/문서/헬스체크)
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(
                "/actuator/**",
                "/swagger-ui/**",
                "/v3/api-docs/**"
        );
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .headers(h -> h.frameOptions(f -> f.disable()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",                 // 루트
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/api/auth/**",      // 로그인/리프레시 등
                                "/api/users/**",      // 회원가입 등 퍼블릭이면 여기 포함
                                "/api/calls/**",
                                "/api/images/**",
                                "/api/likes/**",
                                "/api/matching/**",
                                "/api/reposts/**",
                                "/api/profiles/**",
                                "/api/voice-samples/**",
                                "/api/call-reviews/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex.authenticationEntryPoint(customAuthenticationEntryPoint));

        // 2) RefreshToken 필터 (Bean 재사용) → Username/Password 이전에
        http.addFilterBefore(
                refreshTokenAuthenticationFilter(authenticationManager(http), jwtTokenUtil, objectMapper),
                UsernamePasswordAuthenticationFilter.class
        );

        // 3) JWT 필터
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.authenticationProvider(refreshTokenAuthenticationProvider);
        return builder.build();
    }

    @Bean
    public RefreshTokenAuthenticationFilter refreshTokenAuthenticationFilter(
            AuthenticationManager authenticationManager,
            JwtTokenUtil jwtTokenUtil,
            ObjectMapper objectMapper
    ) {
        // 토큰서비스는 생성자에 이미 포함되어 있음
        return new RefreshTokenAuthenticationFilter(authenticationManager, jwtTokenUtil, objectMapper, tokenService);
    }
}
