package com.siso.like.presentation;

import com.siso.common.response.SisoResponse;
import com.siso.like.application.LikeService;
import com.siso.like.dto.response.LikeResponseDto;
import com.siso.like.dto.response.ReceivedLikeResponseDto;
import com.siso.matching.dto.request.MatchingRequestDto;
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
    public SisoResponse<LikeResponseDto> likeUser(@RequestBody @Valid MatchingRequestDto matchingRequestDto) {

        LikeResponseDto response = likeService.likeUser(matchingRequestDto);
        return SisoResponse.success(response);
    }

    @GetMapping("/received")
    public SisoResponse<List<ReceivedLikeResponseDto>> getReceivedLikes(@RequestParam(name = "receiverId") Long receiverId) {
        List<ReceivedLikeResponseDto> receivedLikes = likeService.getReceivedLikes(receiverId);
        return SisoResponse.success(receivedLikes);
    }
}
