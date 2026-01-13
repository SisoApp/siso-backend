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

import java.util.List;
import java.util.HashSet;
import java.util.Set;

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
    public List<UserInterest> getUserInterestByUserId(User user) {
        return userInterestRepository.findByUserId(user.getId());
    }

    // 사용자의 관심사 선택
    @Transactional
    public void selectUserInterest(Long userId, List<Interest> interests) {
        validateInterestCount(interests);

        userInterestRepository.deleteAllByUserId(userId);

        User userRef = userRepository.getReferenceById(userId); // 프록시만 사용
        List<UserInterest> rows = new HashSet<>(interests).stream()
                .map(i -> UserInterest.builder().user(userRef).interest(i).build())
                .toList();

        userInterestRepository.saveAll(rows);
    }

    // 사용자의 관심사 수정
    @Transactional
    public void updateUserInterest(Long userId, List<Interest> interests) {
        validateInterestCount(interests);

        userInterestRepository.deleteAllByUserId(userId);

        User userRef = userRepository.getReferenceById(userId);
        Set<Interest> unique = new HashSet<>(interests);
        if (unique.isEmpty()) return; // 방어, validate가 막지만 안전하게

        List<UserInterest> rows = unique.stream()
                .map(i -> UserInterest.builder().user(userRef).interest(i).build())
                .toList();

        userInterestRepository.saveAll(rows);
    }

    private void validateInterestCount(List<Interest> interests) {
        if (interests.size() > 5) {
            throw new ExpectedException(ErrorCode.TOO_MANY_INTERESTS);
        } else if (interests.isEmpty()) {
            throw new ExpectedException(ErrorCode.NO_INTERESTS_SELECTED);
        }
    }
}
