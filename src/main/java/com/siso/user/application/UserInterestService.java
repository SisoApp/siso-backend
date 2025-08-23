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
    @Transactional // @Transactional 덕분에 별도의 save() 없이도 DB에 반영
    public void selectUserInterest(User user, List<Interest> interests) {
        validateInterestCount(interests);

        // 중복 제거
        Set<Interest> uniqueInterests = new HashSet<>(interests);
        // 엔티티의 헬퍼 메서드를 호출하여 관계 설정
        uniqueInterests.forEach(user::addInterest);
    }

    // 사용자의 관심사 수정
    @Transactional // @Transactional 덕분에 별도의 save() 없이도 DB에 반영
    public void updateUserInterest(User user, List<Interest> interests) {
        validateInterestCount(interests);

        userInterestRepository.deleteAllByUser(user);

        // 중복 제거
        Set<Interest> uniqueInterests = new HashSet<>(interests);
        List<Interest> uniqueInterestsList = new ArrayList<>(uniqueInterests);

        user.updateUserInterests(uniqueInterestsList);
    }

    private void validateInterestCount(List<Interest> interests) {
        if (interests.size() > 5) {
            throw new ExpectedException(ErrorCode.TOO_MANY_INTERESTS);
        } else if (interests.isEmpty()) {
            throw new ExpectedException(ErrorCode.NO_INTERESTS_SELECTED);
        }
    }
}
