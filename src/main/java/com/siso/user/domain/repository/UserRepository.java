package com.siso.user.domain.repository;

import com.siso.user.domain.model.Provider;
import com.siso.user.domain.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends Repository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.isBlock = false AND u.isDeleted = false")
    Optional<User> findById(@Param("id") Long id);

    @Query("SELECT COUNT(u) > 0 FROM User u " + "WHERE u.id = :id " + "AND u.isBlock = false " + "AND u.isDeleted = false " + "AND u.presenceStatus = 'ONLINE'")
    boolean existsOnlineUserById(@Param("id") Long id);

    boolean existsById(Long userId);

    User save(User user);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.provider = :provider AND u.isDeleted = false AND u.isBlock = false")
    Optional<User> findActiveUserByEmailAndProvider(@Param("email") String email, @Param("provider") Provider provider);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isDeleted = false AND u.isBlock = false")
    Optional<User> findActiveUserByEmail(@Param("email") String email);

    Optional<User> findByEmail(String email);

    Optional<User> findByRefreshToken(String refreshToken);
}
