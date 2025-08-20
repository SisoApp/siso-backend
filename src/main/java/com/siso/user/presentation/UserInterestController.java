package com.siso.user.presentation;

import com.siso.common.response.SisoResponse;
import com.siso.common.web.CurrentUser;
import com.siso.user.application.UserInterestService;
import com.siso.user.domain.model.Interest;
import com.siso.user.domain.model.User;
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
    public SisoResponse<List<UserInterestResponseDto>> getUserInterests(@CurrentUser User user) {
        List<UserInterestResponseDto> interests = userInterestService.getUserInterestByUserId(user)
                .stream()
                .map(userInterest -> new UserInterestResponseDto(userInterest.getInterest()))
                .collect(Collectors.toList());

        return SisoResponse.success(interests);
    }

    // 사용자의 관심사 선택
    @PostMapping("/select")
    public SisoResponse<Void> selectUserInterests(@CurrentUser User user,
                                                  @RequestBody @Valid List<UserInterestRequestDto> interestsDto) {
        List<Interest> interests = interestsDto.stream()
                .map(dto -> Interest.valueOf(dto.getName().toUpperCase()))
                .collect(Collectors.toList());

        userInterestService.selectUserInterest(user, interests);
        return SisoResponse.success(null);
    }

    // 사용자의 관심사 수정
    @PatchMapping("/update")
    public SisoResponse<Void> updateUserInterests(@CurrentUser User user,
                                                  @RequestBody @Valid List<UserInterestRequestDto> interestsDto) {
        List<Interest> interests = interestsDto.stream()
                .map(dto -> Interest.valueOf(dto.getName().toUpperCase()))
                .collect(Collectors.toList());

        userInterestService.updateUserInterest(user, interests);
        return SisoResponse.success(null);
    }
}
