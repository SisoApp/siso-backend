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

        // ✨ 반드시 영속 User + 컬렉션 로드
        User user = userRepository.findByIdWithInterests(userId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));

        // 중복 제거
        Set<Interest> unique = new HashSet<>(interests);

        // 필요 시 기존 유지/추가만 할지, 전체 교체할지 결정
        // 전체 교체면 clear → add
        user.getUserInterests().clear();     // orphanRemoval=true면 DB에서 자동 삭제
        unique.forEach(user::addInterest);   // 영속 상태라 Lazy 예외 없음
    }

    // 사용자의 관심사 수정
    @Transactional
    public void updateUserInterest(Long userId, List<Interest> interests) {
        validateInterestCount(interests);

        User user = userRepository.findByIdWithInterests(userId)
                .orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));

        Set<Interest> unique = new HashSet<>(interests);

        // 리포지토리로 일괄 삭제하는 대신, 컬렉션 조작이 더 자연스러움
        user.getUserInterests().clear();     // orphanRemoval 작동
        unique.forEach(user::addInterest);
    }

    private void validateInterestCount(List<Interest> interests) {
        if (interests.size() > 5) {
            throw new ExpectedException(ErrorCode.TOO_MANY_INTERESTS);
        } else if (interests.isEmpty()) {
            throw new ExpectedException(ErrorCode.NO_INTERESTS_SELECTED);
        }
    }
}
