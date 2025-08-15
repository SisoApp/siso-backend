package com.siso.user.domain.repository;

import com.siso.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    //임시로 만듬 이름과 메소드는 유지해야함
    boolean existsById(Long userId);

    //임시로 만듬 이름과 메소드는 유지해야함
    Optional<User> findById(Long userId);
}
