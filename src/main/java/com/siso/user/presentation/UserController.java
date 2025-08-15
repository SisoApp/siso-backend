package com.siso.user.presentation;

import com.siso.common.response.SisoResponse;
import com.siso.user.application.UserService;
import com.siso.user.dto.request.NotificationRequestDto;
import com.siso.user.dto.response.UserResponseDto;
import com.siso.user.infrastructure.jwt.JwtTokenUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/info")
    public SisoResponse<UserResponseDto> getUserInfo(Authentication authentication) {
        String phoneNumber = authentication.getName();
        UserResponseDto userDto = userService.getUserInfo(phoneNumber);

        return SisoResponse.success(userDto);
    }

    // 알림 동의 수정
    @PatchMapping("/notification")
    public SisoResponse<Void> updateNotificationSubscribed(@RequestBody NotificationRequestDto notificationRequestDto, Authentication authentication) {
        String phoneNumber = authentication.getName();
        userService.updateNotificationSubscribed(phoneNumber, notificationRequestDto.isSubscribed());
        return SisoResponse.success(null);
    }

    // 회원 탈퇴(소프트 삭제)
    @DeleteMapping("/delete")
    public SisoResponse<Void> deleteUser(Authentication authentication) {
        String phoneNumber = authentication.getName();
        userService.deleteUser(phoneNumber);
        return SisoResponse.success(null);
    }

    // 로그아웃
    @PostMapping("/logout")
    public SisoResponse<Void> logout(Authentication authentication, HttpServletResponse httpServletResponse) {
        String phoneNumber = authentication.getName();
        userService.logout(phoneNumber);
        return SisoResponse.success(null);
    }
}
