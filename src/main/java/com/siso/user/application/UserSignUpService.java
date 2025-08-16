package com.siso.user.application;

import com.siso.user.domain.model.Provider;
import com.siso.user.domain.model.User;
import com.siso.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserSignUpService {
    private final UserRepository userRepository;

    public User getOrCreateUser(Provider provider, String phoneNumber) {
        // 1. 해당 제공자와 전화번호로 사용자가 이미 존재하는지 조회합니다.
        return userRepository.findActiveUserByPhoneNumberAndProvider(phoneNumber, provider)
                // 2. 사용자가 없으면 새로 생성하고 저장합니다.
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .provider(provider)
                            .phoneNumber(phoneNumber)
                            .isOnline(true)
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
