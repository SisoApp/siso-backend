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

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public void deleteUser(Long userId) {
        User foundUser = findById(userId);
        foundUser.deleteUser();
    }

    public void registerUser(User user) {
        userRepository.save(user);
    }

    public UserResponseDto getUserProfile(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));

        return new UserResponseDto(user.getProvider(), user.getPhoneNumber());
    }
}
