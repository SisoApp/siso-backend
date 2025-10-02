package com.siso.user.application;

import com.siso.chat.infrastructure.OnlineUserRegistry;
import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.user.domain.model.PresenceStatus;
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
    private final OnlineUserRegistry onlineUserRegistry;


    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));
    }

    public UserResponseDto getUserInfo(User user) {
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
    public void deleteUser(User user) {
        user.deleteUser();
        userRepository.save(user);
    }

    // 하드 삭제 (스케줄러에서 호출)
    @Transactional
    public void hardDeleteUser(User user) {
        if (user.isEligibleForHardDelete()) {
            userRepository.delete(user);
        }
    }

    @Transactional
    public void updateNotificationSubscribed(User user, boolean subscribed) {
        user.updateNotificationSubScribed(subscribed);
        userRepository.save(user);
    }

    @Transactional
    public void logout(User user) {
        user.updateRefreshToken(null);
        user.updatePresenceStatus(PresenceStatus.OFFLINE);
        userRepository.save(user);
    }
}

