package com.siso.user.domain.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import com.siso.user.domain.model.User;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends Repository<User, Long> {
    @Query("SELECT COUNT(u) > 0 FROM User u " + "WHERE u.id = :id " + "AND u.isBlock = false " + "AND u.isDeleted = false " + "AND u.isOnline = true")
    boolean existsOnlineUserById(@Param("id") Long id);

    @Query("SELECT u FROM User u WHERE u.id = :id AND u.isBlock = false AND u.isDeleted = false")
    Optional<User> findById(@Param("id") Long id);

//     //임시로 만듬 이름과 메소드는 유지해야함
//     boolean existsById(Long userId);

//     //임시로 만듬 이름과 메소드는 유지해야함
//     Optional<User> findById(Long userId);  
}
