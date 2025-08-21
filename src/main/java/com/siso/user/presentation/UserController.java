package com.siso.user.presentation;

import com.siso.common.response.SisoResponse;
import com.siso.user.application.UserService;
import com.siso.user.dto.request.NotificationRequestDto;
import com.siso.user.dto.response.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/info")
    public SisoResponse<UserResponseDto> getUserInfo(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        UserResponseDto userDto = userService.getUserInfo(email);
        return SisoResponse.success(userDto);
    }

    // 알림 동의 수정
    @PatchMapping("/notification")
    public SisoResponse<Void> updateNotificationSubscribed(@AuthenticationPrincipal UserDetails userDetails,
                                                           @RequestBody NotificationRequestDto notificationRequestDto) {
        String email = userDetails.getUsername();
        userService.updateNotificationSubscribed(email, notificationRequestDto.isSubscribed());
        return SisoResponse.success(null);
    }

    // 회원 탈퇴(소프트 삭제)
    @DeleteMapping("/delete")
    public SisoResponse<Void> deleteUser(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        userService.deleteUser(email);
        return SisoResponse.success(null);
    }

    @PostMapping("/logout")
    public SisoResponse<Void> logout(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();
        userService.logout(email);
        return SisoResponse.success(null);
    }
}
