package com.siso.user.application;

import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.user.domain.model.User;
import com.siso.user.domain.repository.UserRepository;
import com.siso.user.dto.response.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public void deleteUser(String email) {
        User user = findByEmail(email);
        user.deleteUser();
        userRepository.save(user);
    }

    public UserResponseDto getUserInfo(String email) {
        User user = findByEmail(email);
        return new UserResponseDto(
                user.getId(),
                user.getProvider(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.isDeleted(),
                user.isBlock()
        );
    }

    @Transactional
    public void updateNotificationSubscribed(String email, boolean subscribed) {
        User user = findByEmail(email);
        user.updateNotificationSubScribed(subscribed);
        userRepository.save(user);
    }

    @Transactional
    public void logout(String email) {
        User user = findByEmail(email);
        // 리프레시 토큰 무효화 및 온라인 상태 변경
        user.updateRefreshToken(null);
        user.updateIsOnline(false);
        userRepository.save(user);
    }
}
