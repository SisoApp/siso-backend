package com.siso.user.domain.repository;

import com.siso.user.domain.model.Provider;
import com.siso.user.domain.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends Repository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.isBlock = false AND u.isDeleted = false")
    Optional<User> findById(@Param("id") Long id);

    void save(User user);

    Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findByPhoneNumberAndProvider(@Param("phoneNumber") String phoneNumber, @Param("provider") Provider provider);
}
