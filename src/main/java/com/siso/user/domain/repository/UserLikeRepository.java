package com.siso.user.domain.repository;

import com.siso.user.domain.model.UserLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserLikeRepository extends JpaRepository<UserLike, Long> {
    List<UserLike> findAllBySenderId(Long senderId);

    boolean existsBySenderIdAndReceiverId(Long senderId, Long receiverId);
}
