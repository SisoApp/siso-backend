package com.siso.user.domain.repository;

import com.siso.user.domain.model.Provider;
import com.siso.user.domain.model.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends Repository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.isBlock = false AND u.isDeleted = false")
    Optional<User> findById(@Param("id") Long id);

    boolean existsById(Long userId);

    User save(User user);

    // 하드 삭제용 메서드
    void delete(User user);

    // 하드 삭제 대상 조회용 메서드 (스케줄러에서 사용)
    @Query("SELECT u FROM User u WHERE u.isDeleted = true AND u.deletedAt <= :threshold")
    List<User> findUsersForHardDelete(@Param("threshold") LocalDateTime threshold);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.provider = :provider AND u.isDeleted = false AND u.isBlock = false")
    Optional<User> findActiveUserByEmailAndProvider(@Param("email") String email, @Param("provider") Provider provider);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isBlock = false AND u.isDeleted = false")
    Optional<User> findByEmail(@Param("email")String email);

    Optional<User> findByRefreshToken(String refreshToken);
}
