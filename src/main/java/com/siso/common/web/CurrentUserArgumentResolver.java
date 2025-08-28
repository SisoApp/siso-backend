package com.siso.common.web;

import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.user.domain.model.User;
import com.siso.user.domain.repository.UserRepository;
import com.siso.user.infrastructure.authentication.AccountAdapter;
import com.siso.user.infrastructure.jwt.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // @CurrentUser 붙은 파라미터만 지원
        return parameter.getParameterAnnotation(CurrentUser.class) != null
                && User.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        // 1. 헤더에서 Authorization 가져오기
        String authHeader = webRequest.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            return null; // 헤더 없거나 형식이 잘못되면 null 반환
        }
        System.out.println("=======================authHeader: " + authHeader);
        String refreshToken = authHeader.substring(7);
        System.out.println("=======================refreshToken: " + refreshToken);
        // 2. 토큰 검증 및 payload 추출 (JWT인 경우)
        try {
            // 2. RefreshToken 검증 및 payload에서 이메일 추출
            String email = jwtTokenUtil.getEmailFromToken(refreshToken);
            // 3. DB에서 사용자 조회
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));

            // ★ SecurityContext에 인증 정보 설정
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(user, null, List.of()); // 권한 필요 시 넣기
            SecurityContextHolder.getContext().setAuthentication(auth);

            return user;
        } catch (Exception e) {
            throw new ExpectedException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
    }
}

