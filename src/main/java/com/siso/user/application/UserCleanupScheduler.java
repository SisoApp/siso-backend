package com.siso.user.application;

import com.siso.user.domain.model.User;
import com.siso.user.domain.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserCleanupScheduler {
    private final UserRepository userRepository;
    private final UserService userService;

    // 매일 새벽 3시 실행
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void deleteExpiredUsers() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        List<User> users = userRepository.findUsersForHardDelete(threshold);

        for (User user : users) {
            userService.hardDeleteUser(user); // 실제 DB에서 삭제
        }
    }
}
