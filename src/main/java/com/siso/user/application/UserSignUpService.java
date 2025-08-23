package com.siso.user.application;

import com.siso.user.domain.model.PresenceStatus;
import com.siso.user.domain.model.Provider;
import com.siso.user.domain.model.RegistrationStatus;
import com.siso.user.domain.model.User;
import com.siso.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserSignUpService {
    private final UserRepository userRepository;

    public User getOrCreateUser(Provider provider, String email, String phoneNumber) {
        // 1. 해당 제공자 + 이메일로 활성 유저 조회
        return userRepository.findActiveUserByEmailAndProvider(email, provider)
                .map(user -> {
                    // 기존 유저면 registrationStatus LOGIN으로 세팅
                    user.updateRegistrationStatus(RegistrationStatus.LOGIN);
                    return user;
                })
                .orElseGet(() -> {
                    // 2. 신규 사용자 생성
                    User newUser = User.builder()
                            .provider(provider)
                            .email(email)
                            .phoneNumber(phoneNumber)
                            .presenceStatus(PresenceStatus.ONLINE)
                            .registrationStatus(RegistrationStatus.REGISTER)
                            .notificationSubscribed(false)
                            .isBlock(false)
                            .isDeleted(false)
                            .refreshToken(null)
                            .deletedAt(null)
                            .build();
                    return userRepository.save(newUser);
                });
    }
}

