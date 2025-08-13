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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserInterestService {
    private final UserInterestRepository userInterestRepository;
    private final UserRepository userRepository;

    @Transactional
    public UserInterest findByInterestId(Long id) {
        return userInterestRepository.findById(id).orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));
    }

    public void selectUserInterest(Long userId, List<Interest> interests) {
        validateInterestCount(interests);

        User user = userRepository.findById(userId).orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));

        for (Interest interest : interests) {
            UserInterest userInterest = new UserInterest(user, interest);
            user.addInterest(interest);
            userInterestRepository.save(userInterest);
        }
    }

    public void updateUserInterest(Long userId, List<Interest> interests) {
        validateInterestCount(interests);

        User user = userRepository.findById(userId).orElseThrow(() -> new ExpectedException(ErrorCode.USER_NOT_FOUND));
        userInterestRepository.deleteAllByUser(user);

        List<UserInterest> newUserInterests = interests.stream()
                .map(interest -> new UserInterest(user, interest))
                .collect(Collectors.toList());

        userInterestRepository.saveAll(newUserInterests);
    }

    public List<UserInterest> getUserInterestByUserId(Long userId) {
        return userInterestRepository.findByUserId(userId);
    }

    private void validateInterestCount(List<Interest> interests) {
        if (interests.size() > 5) {
            throw new ExpectedException(ErrorCode.TOO_MANY_INTERESTS);
        } else if (interests.isEmpty()) {
            throw new ExpectedException(ErrorCode.NO_INTERESTS_SELECTED);
        }
    }
}
