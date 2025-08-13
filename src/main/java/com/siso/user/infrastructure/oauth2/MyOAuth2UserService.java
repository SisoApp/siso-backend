package com.siso.user.infrastructure.oauth2;

import com.siso.user.domain.model.Provider;
import com.siso.user.domain.model.User;
import com.siso.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class MyOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        OAuth2UserInfo userInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());

        // phoneNumber와 provider를 기반으로 사용자 조회
        User user = userRepository.findByPhoneNumberAndProvider(userInfo.getPhoneNumber(), Provider.valueOf(registrationId.toUpperCase()))
                .orElse(null);

        if (user == null) {
            // 신규 사용자 회원가입
            user = User.builder()
                    .phoneNumber(userInfo.getPhoneNumber())
                    .provider(Provider.valueOf(registrationId.toUpperCase()))
                    .refreshToken("") // 초기 refreshToken 값 설정
                    .isOnline(true)
                    .isBlock(false)
                    .isDeleted(false)
                    .deletedAt(null)
                    .build();
            userRepository.save(user);
        } else {
            // 기존 사용자 로그인
            user.updateIsOnline(true);
            userRepository.save(user);
        }

        // MyUserDetails는 제거하고 DefaultOAuth2User로 대체
        return new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")), // 사용자 권한을 "ROLE_USER"로 가정
                oAuth2User.getAttributes(),
                userNameAttributeName
        );
    }
}
