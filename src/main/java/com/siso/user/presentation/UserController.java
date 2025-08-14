package com.siso.user.presentation;

import com.siso.common.response.SisoResponse;
import com.siso.user.application.UserService;
import com.siso.user.dto.response.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/profile")
    public SisoResponse<UserResponseDto> getUserProfile(Authentication authentication) {
        String phoneNumber = authentication.getName();
        UserResponseDto userDto = userService.getUserProfile(phoneNumber);

        return SisoResponse.success(userDto);
    }
}
