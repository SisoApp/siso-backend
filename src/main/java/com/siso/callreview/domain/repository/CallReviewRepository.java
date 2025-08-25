package com.siso.callreview.domain.repository;

import com.siso.callreview.domain.model.CallReview;
import com.siso.user.domain.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CallReviewRepository extends JpaRepository<CallReview, Long> {
    // 내가 받은 평가 목록 조회 (내가 receiver일 때)
    @Query("SELECT cr FROM CallReview cr WHERE cr.call.receiver = :user")
    List<CallReview> findAllReceivedReviews(@Param("user") User user);

    // 상대방이 받은 평가 목록 조회
    @Query("SELECT cr FROM CallReview cr WHERE cr.call.receiver = :otherUser")
    List<CallReview> findAllReviewsOfOtherUser(@Param("otherUser") User otherUser);

    // 내가 작성한 평가 목록 조회
    @Query("SELECT cr FROM CallReview cr WHERE cr.call.caller = :user")
    List<CallReview> findAllReviewsWrittenByUser(@Param("user") User user);
}

