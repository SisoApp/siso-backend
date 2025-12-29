package com.siso.user.domain.repository;

import com.siso.config.IntegrationTestBase;
import com.siso.user.domain.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

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
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserRepositoryIntegrationTest extends IntegrationTestBase {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("30일 지난 soft delete 사용자 조회")
    void findUsersForHardDelete_shouldReturnUsersOlderThan30Days() {
        // Given: 35일 전에 삭제된 사용자 생성
        User deletedUser = User.builder()
                .provider(Provider.KAKAO)
                .email("deleted@example.com")
                .phoneNumber("010-1111-2222")
                .presenceStatus(PresenceStatus.OFFLINE)
                .registrationStatus(RegistrationStatus.COMPLETED)
                .isDeleted(true)
                .deletedAt(LocalDateTime.now().minusDays(35))
                .build();

        // Given: 20일 전에 삭제된 사용자 (아직 30일 안 지남)
        User recentDeleted = User.builder()
                .provider(Provider.KAKAO)
                .email("recent@example.com")
                .phoneNumber("010-2222-3333")
                .presenceStatus(PresenceStatus.OFFLINE)
                .registrationStatus(RegistrationStatus.COMPLETED)
                .isDeleted(true)
                .deletedAt(LocalDateTime.now().minusDays(20))
                .build();

        // Given: 활성 사용자
        User activeUser = User.builder()
                .provider(Provider.KAKAO)
                .email("active@example.com")
                .phoneNumber("010-3333-4444")
                .presenceStatus(PresenceStatus.ONLINE)
                .registrationStatus(RegistrationStatus.COMPLETED)
                .build();

        userRepository.save(deletedUser);
        userRepository.save(recentDeleted);
        userRepository.save(activeUser);

        // When: 30일 지난 사용자 조회
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        List<User> users = userRepository.findUsersForHardDelete(threshold);

        // Then: 35일 전에 삭제된 사용자만 조회됨
        assertThat(users).isNotEmpty();
        assertThat(users).anyMatch(u -> u.getEmail().equals("deleted@example.com"));
        assertThat(users).noneMatch(u -> u.getEmail().equals("recent@example.com"));
        assertThat(users).noneMatch(u -> u.getEmail().equals("active@example.com"));
    }

    @Test
    @DisplayName("이메일로 활성 사용자 조회 성공")
    void findByEmail_whenActiveUser_shouldReturnUser() {
        // Given: 활성 사용자
        User user = User.builder()
                .provider(Provider.KAKAO)
                .email("test@example.com")
                .phoneNumber("010-1234-5678")
                .presenceStatus(PresenceStatus.ONLINE)
                .registrationStatus(RegistrationStatus.COMPLETED)
                .build();

        userRepository.save(user);

        // When: 이메일로 조회
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // Then: 사용자가 조회됨
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        assertThat(found.get().getProvider()).isEqualTo(Provider.KAKAO);
    }

    @Test
    @DisplayName("이메일로 조회 - 삭제된 사용자는 조회 안됨")
    void findByEmail_whenDeletedUser_shouldReturnEmpty() {
        // Given: 삭제된 사용자
        User deletedUser = User.builder()
                .provider(Provider.KAKAO)
                .email("deleted@example.com")
                .phoneNumber("010-1234-5678")
                .presenceStatus(PresenceStatus.OFFLINE)
                .registrationStatus(RegistrationStatus.COMPLETED)
                .isDeleted(true)
                .deletedAt(LocalDateTime.now())
                .build();

        userRepository.save(deletedUser);

        // When: 이메일로 조회
        Optional<User> found = userRepository.findByEmail("deleted@example.com");

        // Then: 조회되지 않음 (isDeleted = true)
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("이메일과 Provider로 활성 사용자 조회")
    void findActiveUserByEmailAndProvider_shouldReturnActiveUser() {
        // Given: 카카오 활성 사용자
        User kakaoUser = User.builder()
                .provider(Provider.KAKAO)
                .email("test@example.com")
                .phoneNumber("010-1234-5678")
                .presenceStatus(PresenceStatus.ONLINE)
                .registrationStatus(RegistrationStatus.COMPLETED)
                .build();

        // Given: 애플 활성 사용자 (같은 이메일)
        User appleUser = User.builder()
                .provider(Provider.APPLE)
                .email("test@example.com")
                .phoneNumber("010-1234-5678")
                .presenceStatus(PresenceStatus.ONLINE)
                .registrationStatus(RegistrationStatus.COMPLETED)
                .build();

        userRepository.save(kakaoUser);
        userRepository.save(appleUser);

        // When: 카카오 사용자 조회
        Optional<User> found = userRepository.findActiveUserByEmailAndProvider("test@example.com", Provider.KAKAO);

        // Then: 카카오 사용자만 조회됨
        assertThat(found).isPresent();
        assertThat(found.get().getProvider()).isEqualTo(Provider.KAKAO);
    }

    @Test
    @DisplayName("사용자 soft delete 테스트")
    void deleteUser_shouldSetIsDeletedTrue() {
        // Given: 활성 사용자
        User user = User.builder()
                .provider(Provider.KAKAO)
                .email("test@example.com")
                .phoneNumber("010-1234-5678")
                .presenceStatus(PresenceStatus.ONLINE)
                .registrationStatus(RegistrationStatus.COMPLETED)
                .build();

        User saved = userRepository.save(user);
        Long userId = saved.getId();

        // When: soft delete 수행
        saved.deleteUser();
        userRepository.save(saved);

        // Then: isDeleted = true, deletedAt 설정됨
        Optional<User> deletedUser = userRepository.findByEmail("test@example.com");
        assertThat(deletedUser).isEmpty();  // findByEmail은 isDeleted = false인 사용자만 조회

        // DB에서 직접 조회하면 존재함 (hard delete 아님)
        assertThat(userRepository.existsById(userId)).isTrue();
    }

    @Test
    @DisplayName("30일 이내 soft delete 사용자 재활성화 가능")
    void reActivateUser_shouldSetIsDeletedFalse() {
        // Given: soft delete된 사용자
        User user = User.builder()
                .provider(Provider.KAKAO)
                .email("test@example.com")
                .phoneNumber("010-1234-5678")
                .presenceStatus(PresenceStatus.OFFLINE)
                .registrationStatus(RegistrationStatus.COMPLETED)
                .isDeleted(true)
                .deletedAt(LocalDateTime.now().minusDays(10))
                .build();

        User saved = userRepository.save(user);

        // When: 재활성화
        saved.reActivateUser();
        userRepository.save(saved);

        // Then: isDeleted = false, deletedAt = null
        Optional<User> reactivated = userRepository.findByEmail("test@example.com");
        assertThat(reactivated).isPresent();
        assertThat(reactivated.get().isDeleted()).isFalse();
        assertThat(reactivated.get().getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("30일 지난 사용자 hard delete 가능 여부 확인")
    void isEligibleForHardDelete_shouldReturnTrueAfter30Days() {
        // Given: 35일 전에 삭제된 사용자
        User user = User.builder()
                .provider(Provider.KAKAO)
                .email("test@example.com")
                .phoneNumber("010-1234-5678")
                .presenceStatus(PresenceStatus.OFFLINE)
                .registrationStatus(RegistrationStatus.COMPLETED)
                .isDeleted(true)
                .deletedAt(LocalDateTime.now().minusDays(35))
                .build();

        // When & Then: 30일 지났으므로 hard delete 가능
        assertThat(user.isEligibleForHardDelete()).isTrue();

        // Given: 20일 전에 삭제된 사용자
        User recentDeleted = User.builder()
                .provider(Provider.KAKAO)
                .email("recent@example.com")
                .phoneNumber("010-2222-3333")
                .presenceStatus(PresenceStatus.OFFLINE)
                .registrationStatus(RegistrationStatus.COMPLETED)
                .isDeleted(true)
                .deletedAt(LocalDateTime.now().minusDays(20))
                .build();

        // When & Then: 30일 안 지났으므로 hard delete 불가
        assertThat(recentDeleted.isEligibleForHardDelete()).isFalse();
    }

    @Test
    @DisplayName("리프레시 토큰으로 사용자 조회")
    void findByRefreshToken_shouldReturnUser() {
        // Given: 리프레시 토큰이 있는 사용자
        String refreshToken = "test-refresh-token-12345";

        User user = User.builder()
                .provider(Provider.KAKAO)
                .email("test@example.com")
                .phoneNumber("010-1234-5678")
                .presenceStatus(PresenceStatus.ONLINE)
                .registrationStatus(RegistrationStatus.COMPLETED)
                .refreshToken(refreshToken)
                .build();

        userRepository.save(user);

        // When: 리프레시 토큰으로 조회
        Optional<User> found = userRepository.findByRefreshToken(refreshToken);

        // Then: 사용자가 조회됨
        assertThat(found).isPresent();
        assertThat(found.get().getRefreshToken()).isEqualTo(refreshToken);
    }
}
