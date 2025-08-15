package com.siso.user.infrastructure.oauth2;

import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.user.application.UserSignUpService;
import com.siso.user.domain.model.Provider;
import com.siso.user.domain.model.User;
import com.siso.user.infrastructure.authentication.AccountAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MyOAuth2UserService extends DefaultOAuth2UserService {
    private final UserSignUpService userSignUpService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oAuth2User.getAttributes());

        String phoneNumber = oAuth2UserInfo.getPhoneNumber();
        if (phoneNumber == null) {
            throw new ExpectedException(ErrorCode.OAUTH2_PHONE_NUMBER_NOT_FOUND);
        }
        Provider provider = Provider.valueOf(registrationId.toUpperCase());

        User user = userSignUpService.getOrCreateUser(provider, phoneNumber);

        return new AccountAdapter(user, oAuth2User.getAttributes());
    }
}
