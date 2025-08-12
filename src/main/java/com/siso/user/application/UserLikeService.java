package com.siso.user.application;

import com.siso.user.domain.model.UserLike;
import com.siso.user.domain.repository.UserLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserLikeService {
    private final UserLikeRepository userLikeRepository;

//    public UserLike findByUserLikeId(Long id) {
//        return userLikeRepository.findById(id).
//    }

}
