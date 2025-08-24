package com.siso.like.presentation;

import com.siso.common.response.SisoResponse;
import com.siso.common.web.CurrentUser;
import com.siso.like.application.LikeService;
import com.siso.like.dto.request.LikeRequestDto;
import com.siso.like.dto.response.LikeResponseDto;
import com.siso.like.dto.response.ReceivedLikeResponseDto;
import com.siso.user.domain.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {
    private final LikeService likeService;

    @PostMapping
    public SisoResponse<LikeResponseDto> likeUser(@CurrentUser User user,
                                                  @RequestBody @Valid LikeRequestDto likeRequestDto) {

        LikeResponseDto response = likeService.likeUser(user, likeRequestDto);
        return SisoResponse.success(response);
    }

    @GetMapping("/received")
    public SisoResponse<List<ReceivedLikeResponseDto>> getReceivedLikes(@CurrentUser User user) {
        List<ReceivedLikeResponseDto> receivedLikes = likeService.getReceivedLikes(user);
        return SisoResponse.success(receivedLikes);
    }
}
