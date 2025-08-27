package com.siso.user.presentation;

import com.siso.common.response.SisoResponse;
import com.siso.common.web.CurrentUser;
import com.siso.user.application.UserFilterService;
import com.siso.user.domain.model.User;
import com.siso.user.dto.response.FilteredUserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserFilterController {
    private final UserFilterService userFilterService;

    // 필터링된 사용자 목록 조회 (PreferenceSex 기준 + 관심사 많이 겹치는 순)
    @GetMapping("/filtered")
    public SisoResponse<List<FilteredUserResponseDto>> getFilteredUsers(@CurrentUser User user) {
        List<FilteredUserResponseDto> filteredUsers = userFilterService.getFilteredUsers(user);
        return SisoResponse.success(filteredUsers);
    }
}
