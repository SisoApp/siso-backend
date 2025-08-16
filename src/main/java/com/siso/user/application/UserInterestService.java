package com.siso.user.application;

import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.user.domain.model.Interest;
import com.siso.user.domain.model.User;
import com.siso.user.domain.model.UserInterest;
import com.siso.user.domain.repository.UserInterestRepository;
import com.siso.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserInterestService {
    private final UserInterestRepository userInterestRepository;
    private final UserRepository userRepository;

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));
    }

    // 사용자의 관심사 목록 조회
    @Transactional(readOnly = true)
    public List<UserInterest> getUserInterestByUserId(Long userId) {
        return userInterestRepository.findByUserId(userId);
    }

    // 사용자의 관심사 선택
    @Transactional
    public void selectUserInterest(Long userId, List<Interest> interests) {
        validateInterestCount(interests);
        User user = findById(userId);

        // 중복 제거
        Set<Interest> uniqueInterests = new HashSet<>(interests);
        List<UserInterest> newUserInterests = uniqueInterests.stream()
                .map(interest -> new UserInterest(user, interest))
                .toList();

        userInterestRepository.saveAll(newUserInterests);
    }

    // 사용자의 관심사 수정
    @Transactional
    public void updateUserInterest(Long userId, List<Interest> interests) {
        validateInterestCount(interests);
        User user = findById(userId);
        userInterestRepository.deleteAllByUser(user);

        // 중복 제거
        Set<Interest> uniqueInterests = new HashSet<>(interests);
        List<UserInterest> newUserInterests = uniqueInterests.stream()
                .map(interest -> new UserInterest(user, interest))
                .toList();

        userInterestRepository.saveAll(newUserInterests);
    }

    private void validateInterestCount(List<Interest> interests) {
        if (interests.size() > 5) {
            throw new ExpectedException(ErrorCode.TOO_MANY_INTERESTS);
        } else if (interests.isEmpty()) {
            throw new ExpectedException(ErrorCode.NO_INTERESTS_SELECTED);
        }
    }
}
