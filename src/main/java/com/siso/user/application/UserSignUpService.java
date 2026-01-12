package com.siso.user.application;

import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.user.domain.model.PresenceStatus;
import com.siso.user.domain.model.Provider;
import com.siso.user.domain.model.RegistrationStatus;
import com.siso.user.domain.model.User;
import com.siso.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserSignUpService {
    private final UserRepository userRepository;

    public User getOrCreateUser(Provider provider, String email, String phoneNumber) {
        // 1. 해당 제공자 + 이메일로 활성 유저 조회
        return userRepository.findActiveUserByEmailAndProvider(email, provider)
                .map(user -> {
                    if (user.isDeleted()) {
                        // 30일 이내라면 복구
                        if (user.getDeletedAt().isAfter(LocalDateTime.now().minusDays(30))) {
                            user.reActivateUser();
                            return user;
                        } else {
                            // 30일 지났으면 로그인 불가 → 앱에서 회원가입 유도
                            throw new ExpectedException(ErrorCode.USER_NOT_FOUND_OR_DELETED);
                        }
                    }
                    // 기존 유저면 로그인 상태로 세팅
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
                            .lastActiveAt(null)
                            .build();
                    return userRepository.save(newUser);
                });
    }
}

