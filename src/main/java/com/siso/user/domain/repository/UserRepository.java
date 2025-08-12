package com.siso.user.domain.repository;

import com.siso.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.isBlock = false AND u.isDeleted = false")
    List<User> findAllUsers();
}
