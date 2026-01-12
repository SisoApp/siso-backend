package com.siso.user.presentation;

import com.siso.common.response.SisoResponse;
import com.siso.common.web.CurrentUser;
import com.siso.user.application.UserService;
import com.siso.user.domain.model.User;
import com.siso.user.dto.request.NotificationRequestDto;
import com.siso.user.dto.response.UserResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping(value = "/info", produces = "application/json; charset=UTF-8")
    public SisoResponse<UserResponseDto> getUserInfo(@CurrentUser User user) {
        UserResponseDto userDto = userService.getUserInfo(user);
        return SisoResponse.success(userDto);
    }

    // 알림 동의 수정
    @PatchMapping(value = "/notification", produces = "application/json; charset=UTF-8")
    public SisoResponse<Void> updateNotificationSubscribed(@CurrentUser User user,
                                                           @Valid @RequestBody NotificationRequestDto notificationRequestDto) {
        userService.updateNotificationSubscribed(user, notificationRequestDto.isSubscribed());
        return SisoResponse.success(null);
    }

    // 회원 탈퇴(소프트 삭제)
    @DeleteMapping(value = "/delete", produces = "application/json; charset=UTF-8")
    public SisoResponse<Void> deleteUser(@CurrentUser User user) {
        userService.deleteUser(user);
        return SisoResponse.success(null);
    }

    @PostMapping(value = "/logout", produces = "application/json; charset=UTF-8")
    public SisoResponse<Void> logout(@CurrentUser User user) {
        userService.logout(user);
        return SisoResponse.success(null);
    }
}
