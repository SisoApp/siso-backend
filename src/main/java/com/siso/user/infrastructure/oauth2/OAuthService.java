package com.siso.user.infrastructure.oauth2;

import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.user.application.UserProfileService;
import com.siso.user.application.UserSignUpService;
import com.siso.user.domain.model.Provider;
import com.siso.user.domain.model.RegistrationStatus;
import com.siso.user.domain.model.User;
import com.siso.user.domain.repository.UserRepository;
import com.siso.user.dto.response.TokenResponseDto;
import com.siso.user.infrastructure.jwt.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OAuthService {
    private final UserSignUpService userSignUpService;
    private final UserProfileService userProfileService;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;
    private final OAuthProviderClientFactory clientFactory; // Kakao/Apple REST 호출용

    public TokenResponseDto loginWithProvider(String providerName, String codeOrAccessToken) {
        Map<String, Object> attributes = clientFactory.getClient(providerName).getUserAttributes(codeOrAccessToken);
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(providerName, attributes);

        String email = oAuth2UserInfo.getEmail();
        String phoneNumber = oAuth2UserInfo.getPhoneNumber();
        if (email == null) throw new ExpectedException(ErrorCode.OAUTH2_EMAIL_NOT_FOUND);

        Provider provider = Provider.valueOf(providerName.toUpperCase());
        User user = userSignUpService.getOrCreateUser(provider, email, phoneNumber);
        System.out.println("User saved: " + user.getEmail() + ", " + user.getPhoneNumber());

        String jwtAccessToken = jwtTokenUtil.generateAccessToken(user.getEmail());
        String jwtRefreshToken = jwtTokenUtil.generateRefreshToken(user.getEmail());
        System.out.println("====================wtAccessToken: " + jwtAccessToken);
        System.out.println("====================jwtRefreshToken: " + jwtRefreshToken);

        user.updateRefreshToken(jwtRefreshToken);
        userRepository.save(user);

        boolean hasProfile = userProfileService.existsByUserId(user.getId());
        return new TokenResponseDto(jwtAccessToken, jwtRefreshToken, user.getRegistrationStatus(), hasProfile);
    }
}
