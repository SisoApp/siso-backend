package com.siso.integrationTest.repository;

import com.siso.integrationTest.config.RepositoryTestBase;
import com.siso.user.domain.model.*;
import com.siso.user.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * UserRepository 통합 테스트
 *
 * 테스트 대상:
 * - 30일 지난 soft delete 사용자 조회
 * - N+1 문제 없이 사용자와 이미지, 프로필 조회
 * - 이메일과 Provider로 활성 사용자 조회
 */
@DisplayName("UserRepository 통합 테스트")
class UserRepositoryIntegrationTest extends RepositoryTestBase {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("30일 지난 soft delete 사용자 조회")
    void findUsersForHardDelete_shouldReturnUsersOlderThan30Days() {
        User deletedUser = User.builder()
                .provider(Provider.KAKAO)
                .email("deleted@example.com")
                .phoneNumber("010-1111-2222")
                .presenceStatus(PresenceStatus.OFFLINE)
                .registrationStatus(RegistrationStatus.LOGIN)
                .isDeleted(true)
                .deletedAt(LocalDateTime.now().minusDays(35))
                .build();

        User recentDeleted = User.builder()
                .provider(Provider.KAKAO)
                .email("recent@example.com")
                .phoneNumber("010-2222-3333")
                .presenceStatus(PresenceStatus.OFFLINE)
                .registrationStatus(RegistrationStatus.LOGIN)
                .isDeleted(true)
                .deletedAt(LocalDateTime.now().minusDays(20))
                .build();

        User activeUser = User.builder()
                .provider(Provider.KAKAO)
                .email("active@example.com")
                .phoneNumber("010-3333-4444")
                .presenceStatus(PresenceStatus.ONLINE)
                .registrationStatus(RegistrationStatus.LOGIN)
                .build();

        userRepository.save(deletedUser);
        userRepository.save(recentDeleted);
        userRepository.save(activeUser);

        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        List<User> users = userRepository.findUsersForHardDelete(threshold);

        assertThat(users).isNotEmpty();
        assertThat(users).anyMatch(u -> u.getEmail().equals("deleted@example.com"));
        assertThat(users).noneMatch(u -> u.getEmail().equals("recent@example.com"));
        assertThat(users).noneMatch(u -> u.getEmail().equals("active@example.com"));
    }

    @Test
    @DisplayName("이메일로 활성 사용자 조회 성공")
    void findByEmail_whenActiveUser_shouldReturnUser() {
        User user = User.builder()
                .provider(Provider.KAKAO)
                .email("test@example.com")
                .phoneNumber("010-1234-5678")
                .presenceStatus(PresenceStatus.ONLINE)
                .registrationStatus(RegistrationStatus.LOGIN)
                .build();

        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("test@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        assertThat(found.get().getProvider()).isEqualTo(Provider.KAKAO);
    }

    @Test
    @DisplayName("이메일로 조회 - 삭제된 사용자는 조회 안됨")
    void findByEmail_whenDeletedUser_shouldReturnEmpty() {
        User deletedUser = User.builder()
                .provider(Provider.KAKAO)
                .email("deleted@example.com")
                .phoneNumber("010-1234-5678")
                .presenceStatus(PresenceStatus.OFFLINE)
                .registrationStatus(RegistrationStatus.LOGIN)
                .isDeleted(true)
                .deletedAt(LocalDateTime.now())
                .build();

        userRepository.save(deletedUser);

        Optional<User> found = userRepository.findByEmail("deleted@example.com");

        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("이메일과 Provider로 활성 사용자 조회")
    void findActiveUserByEmailAndProvider_shouldReturnActiveUser() {
        User kakaoUser = User.builder()
                .provider(Provider.KAKAO)
                .email("test@example.com")
                .phoneNumber("010-1234-5678")
                .presenceStatus(PresenceStatus.ONLINE)
                .registrationStatus(RegistrationStatus.LOGIN)
                .build();

        User appleUser = User.builder()
                .provider(Provider.APPLE)
                .email("test@example.com")
                .phoneNumber("010-1234-5678")
                .presenceStatus(PresenceStatus.ONLINE)
                .registrationStatus(RegistrationStatus.LOGIN)
                .build();

        userRepository.save(kakaoUser);
        userRepository.save(appleUser);

        Optional<User> found = userRepository.findActiveUserByEmailAndProvider("test@example.com", Provider.KAKAO);

        assertThat(found).isPresent();
        assertThat(found.get().getProvider()).isEqualTo(Provider.KAKAO);
    }

    @Test
    @DisplayName("사용자 soft delete 테스트")
    void deleteUser_shouldSetIsDeletedTrue() {
        User user = User.builder()
                .provider(Provider.KAKAO)
                .email("test@example.com")
                .phoneNumber("010-1234-5678")
                .presenceStatus(PresenceStatus.ONLINE)
                .registrationStatus(RegistrationStatus.LOGIN)
                .build();

        User saved = userRepository.save(user);
        Long userId = saved.getId();

        saved.deleteUser();
        userRepository.save(saved);

        Optional<User> deletedUser = userRepository.findByEmail("test@example.com");
        assertThat(deletedUser).isEmpty();

        assertThat(userRepository.existsById(userId)).isTrue();
    }

    @Test
    @DisplayName("30일 이내 soft delete 사용자 재활성화 가능")
    void reActivateUser_shouldSetIsDeletedFalse() {
        User user = User.builder()
                .provider(Provider.KAKAO)
                .email("test@example.com")
                .phoneNumber("010-1234-5678")
                .presenceStatus(PresenceStatus.OFFLINE)
                .registrationStatus(RegistrationStatus.LOGIN)
                .isDeleted(true)
                .deletedAt(LocalDateTime.now().minusDays(10))
                .build();

        User saved = userRepository.save(user);

        saved.reActivateUser();
        userRepository.save(saved);

        Optional<User> reactivated = userRepository.findByEmail("test@example.com");
        assertThat(reactivated).isPresent();
        assertThat(reactivated.get().isDeleted()).isFalse();
        assertThat(reactivated.get().getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("30일 지난 사용자 hard delete 가능 여부 확인")
    void isEligibleForHardDelete_shouldReturnTrueAfter30Days() {
        User user = User.builder()
                .provider(Provider.KAKAO)
                .email("test@example.com")
                .phoneNumber("010-1234-5678")
                .presenceStatus(PresenceStatus.OFFLINE)
                .registrationStatus(RegistrationStatus.LOGIN)
                .isDeleted(true)
                .deletedAt(LocalDateTime.now().minusDays(35))
                .build();

        assertThat(user.isEligibleForHardDelete()).isTrue();

        User recentDeleted = User.builder()
                .provider(Provider.KAKAO)
                .email("recent@example.com")
                .phoneNumber("010-2222-3333")
                .presenceStatus(PresenceStatus.OFFLINE)
                .registrationStatus(RegistrationStatus.LOGIN)
                .isDeleted(true)
                .deletedAt(LocalDateTime.now().minusDays(20))
                .build();

        assertThat(recentDeleted.isEligibleForHardDelete()).isFalse();
    }

    @Test
    @DisplayName("리프레시 토큰으로 사용자 조회")
    void findByRefreshToken_shouldReturnUser() {
        String refreshToken = "test-refresh-token-12345";

        User user = User.builder()
                .provider(Provider.KAKAO)
                .email("test@example.com")
                .phoneNumber("010-1234-5678")
                .presenceStatus(PresenceStatus.ONLINE)
                .registrationStatus(RegistrationStatus.LOGIN)
                .refreshToken(refreshToken)
                .build();

        userRepository.save(user);

        Optional<User> found = userRepository.findByRefreshToken(refreshToken);

        assertThat(found).isPresent();
        assertThat(found.get().getRefreshToken()).isEqualTo(refreshToken);
    }
}
