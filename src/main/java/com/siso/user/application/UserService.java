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

    public User findByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public void deleteUser(String phoneNumber) {
        User user = findByPhoneNumber(phoneNumber);
        user.deleteUser();
        userRepository.save(user);
    }

    public UserResponseDto getUserInfo(String phoneNumber) {
        User user = findByPhoneNumber(phoneNumber);
        return new UserResponseDto(user.getProvider(), user.getPhoneNumber());
    }

    @Transactional
    public void updateNotificationSubscribed(String phoneNumber, boolean subscribed) {
        User user = findByPhoneNumber(phoneNumber);
        user.updateNotificationSubScribed(subscribed);
        userRepository.save(user);
    }

    @Transactional
    public void logout(String phoneNumber) {
        User user = findByPhoneNumber(phoneNumber);
        // 리프레시 토큰 무효화 및 온라인 상태 변경
        user.updateRefreshToken(null);
        user.updateIsOnline(false);
        userRepository.save(user);
    }
}
