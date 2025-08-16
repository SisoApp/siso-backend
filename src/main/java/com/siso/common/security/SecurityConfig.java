package com.siso.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siso.user.infrastructure.jwt.*;
import com.siso.user.infrastructure.oauth2.MyOAuth2UserService;
import com.siso.user.infrastructure.oauth2.OAuth2LoginSuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

import java.util.Map;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final MyOAuth2UserService myOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final JwtRequestFilter jwtRequestFilter;
    private final RefreshTokenAuthenticationProvider refreshTokenAuthenticationProvider;
    private final JwtTokenUtil jwtTokenUtil;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .headers(x -> x.frameOptions(y -> y.disable()))
            .authorizeHttpRequests(auth -> auth
                // 공개적으로 접근 가능한 엔드포인트
                .requestMatchers(
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/oauth2/**",
                        "/login/oauth2/**", // OAuth2 로그인 콜백 경로
                        "/api/users/**",
                        "/",
                        "/api/auth/refresh" // 토큰 재발급 경로
                ).permitAll()
                // 특정 권한이 필요한 엔드포인트 (필요시 추가)
                .requestMatchers(("/api/users/test")).hasAuthority("ROLE_USER")
                // 위에서 허용된 경로를 제외한 모든 요청은 인증 필요
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                    .authorizationEndpoint(endpoint -> endpoint
                            .baseUri("/oauth2/authorization") // 소셜 로그인 시작 URL
                    )
                    .redirectionEndpoint(endpoint -> endpoint
                            .baseUri("/login/oauth2/code/*") // 리디렉션 URL
                    )
                    .userInfoEndpoint(endpoint -> endpoint
                            .userService(myOAuth2UserService) // 사용자 정보 처리 서비스
                    )
                    .successHandler(oAuth2LoginSuccessHandler) // 로그인 성공 시 핸들러
            )
            .logout(logout -> logout
                    .logoutUrl("/api/users/logout")
                    .logoutSuccessHandler((request, response, authentication) -> {
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        objectMapper.writeValue(response.getWriter(), Map.of("message", "로그아웃 성공"));
                    })
            )
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .exceptionHandling(exception -> exception
                    .authenticationEntryPoint(customAuthenticationEntryPoint) // 인증 실패 시 핸들러
            );

        // JWT 필터 추가: 요청 헤더에서 JWT를 검증
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        // Refresh Token 필터 추가: Refresh Token으로 Access Token 재발급
        http.addFilterBefore(
                new RefreshTokenAuthenticationFilter(authenticationManager(http), jwtTokenUtil, objectMapper),
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }

    @Bean
    public LogoutSuccessHandler logoutSuccessHandler() {
        SimpleUrlLogoutSuccessHandler handler = new SimpleUrlLogoutSuccessHandler();
        handler.setDefaultTargetUrl("/"); // 로그아웃 성공 후 리다이렉트될 페이지
        return handler;
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        // Refresh Token 인증을 위한 provider 등록
        authenticationManagerBuilder.authenticationProvider(refreshTokenAuthenticationProvider);
        return authenticationManagerBuilder.build();
    }

    @Bean
    public RefreshTokenAuthenticationFilter refreshTokenAuthenticationFilter(AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenUtil, ObjectMapper objectMapper) {
        return new RefreshTokenAuthenticationFilter(authenticationManager, jwtTokenUtil, objectMapper);
    }
}