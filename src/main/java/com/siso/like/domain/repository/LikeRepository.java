package com.siso.like.domain.repository;

import com.siso.like.domain.model.Like;
import com.siso.like.domain.model.LikeStatus;
import com.siso.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findBySenderAndReceiver(User sender, User receiver);

    List<Like> findAllByReceiverAndLikeStatus(User receiver, LikeStatus likeStatus);

    boolean existsBySenderAndReceiverAndLikeStatus(User sender, User receiver, LikeStatus likeStatus);
}
