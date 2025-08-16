package com.siso.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siso.user.infrastructure.cookie.HttpCookieOAuth2AuthorizationRequestRepository;
import com.siso.user.infrastructure.jwt.*;
import com.siso.user.infrastructure.oauth2.MyOAuth2UserService;
import com.siso.user.infrastructure.oauth2.OAuth2AuthenticationFailureHandler;
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
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
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
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .headers(x -> x.frameOptions(y -> y.disable()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/api/users/**",
                                "/",
                                "/api/auth/refresh"
                        ).permitAll()
                        .requestMatchers("/api/users/test").hasAuthority("ROLE_USER")
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(endpoint -> endpoint
                                .baseUri("/oauth2/authorization")
                                .authorizationRequestRepository(httpCookieOAuth2AuthorizationRequestRepository)
                        )
                        .redirectionEndpoint(endpoint -> endpoint
                                .baseUri("/login/oauth2/code/*")
                        )
                        .userInfoEndpoint(endpoint -> endpoint
                                .userService(myOAuth2UserService)
                        )
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureHandler(oauth2AuthenticationFailureHandler())
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
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

    // OAuth2 로그인 실패 핸들러 Bean 등록
    @Bean
    public AuthenticationFailureHandler oauth2AuthenticationFailureHandler() {
        return new OAuth2AuthenticationFailureHandler();
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