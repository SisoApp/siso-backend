package com.siso.user.presentation;

import com.siso.common.response.SisoResponse;
import com.siso.user.application.UserInterestService;
import com.siso.user.domain.model.Interest;
import com.siso.user.dto.request.UserInterestRequestDto;
import com.siso.user.dto.response.UserInterestResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/{userId}/interests")
public class UserInterestController {
    private final UserInterestService userInterestService;

    // 사용자의 관심사 목록 조회
    @GetMapping("/list")
    public SisoResponse<List<UserInterestResponseDto>> getUserInterests(@PathVariable(name = "userId") Long userId) {
        List<UserInterestResponseDto> interests = userInterestService.getUserInterestByUserId(userId)
                .stream()
                .map(userInterest -> new UserInterestResponseDto(userInterest.getInterest()))
                .collect(Collectors.toList());

        return SisoResponse.success(interests);
    }

    // 사용자의 관심사 선택
    @PostMapping("/select")
    public SisoResponse<Void> selectUserInterests(@PathVariable(name = "userId") Long userId, @RequestBody @Valid List<UserInterestRequestDto> interestsDto) {
        List<Interest> interests = interestsDto.stream()
                .map(UserInterestRequestDto::getInterest)
                .collect(Collectors.toList());

        userInterestService.selectUserInterest(userId, interests);
        return SisoResponse.success(null);
    }

    // 사용자의 관심사 수정
    @PatchMapping("/update")
    public SisoResponse<Void> updateUserInterests(@PathVariable(name = "userId") Long userId, @RequestBody @Valid List<UserInterestRequestDto> interestsDto) {
        List<Interest> interests = interestsDto.stream()
                .map(UserInterestRequestDto::getInterest) // enum 그대로 꺼내옴
                .collect(Collectors.toList());

        userInterestService.updateUserInterest(userId, interests);
        return SisoResponse.success(null);
    }
}
