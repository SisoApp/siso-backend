package com.siso.user.application;

import com.siso.user.domain.model.Provider;
import com.siso.user.domain.model.User;
import com.siso.user.domain.repository.UserRepository;
import com.siso.user.infrastructure.oauth2.OAuth2UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserSignUpService {
    private final UserRepository userRepository;

    @Transactional
    public User saveOrUpdateUser(OAuth2UserInfo userInfo) {
        // 전화번호와 제공자로 사용자 조회
        User user = userRepository.findByPhoneNumberAndProvider(userInfo.getPhoneNumber(), Provider.valueOf(userInfo.getProvider()))
                .orElse(null);

        if (user == null) {
            // 신규 사용자 회원가입
            user = User.builder()
                    .phoneNumber(userInfo.getPhoneNumber())
                    .provider(Provider.valueOf(userInfo.getProvider()))
                    .refreshToken("") // 초기 refreshToken 값 설정
                    .isOnline(true)
                    .isBlock(false)
                    .isDeleted(false)
                    .deletedAt(null)
                    .build();
            userRepository.save(user);
        } else {
            // 기존 사용자 로그인: isOnline 상태 업데이트
            user.updateIsOnline(true);
            userRepository.save(user);
        }

        return user;
    }
}
