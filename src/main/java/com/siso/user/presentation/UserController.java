package com.siso.user.presentation;

import com.siso.common.response.SisoResponse;
import com.siso.common.web.CurrentUser;
import com.siso.user.application.UserService;
import com.siso.user.domain.model.User;
import com.siso.user.dto.request.NotificationRequestDto;
import com.siso.user.dto.response.UserResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "사용자 관리 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {
    private final UserService userService;

    @Operation(summary = "내 정보 조회", description = "로그인한 사용자의 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
            @ApiResponse(responseCode = "404", description = "사용자 없음", content = @Content)
    })
    @GetMapping(value = "/info", produces = "application/json; charset=UTF-8")
    public SisoResponse<UserResponseDto> getUserInfo(
            @Parameter(hidden = true) @CurrentUser User user
    ) {
        UserResponseDto userDto = userService.getUserInfo(user);
        return SisoResponse.success(userDto);
    }

    @Operation(summary = "알림 설정 변경", description = "푸시 알림 수신 동의 여부를 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    })
    @PatchMapping(value = "/notification", produces = "application/json; charset=UTF-8")
    public SisoResponse<Void> updateNotificationSubscribed(
            @Parameter(hidden = true) @CurrentUser User user,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "알림 설정 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = NotificationRequestDto.class))
            )
            @Valid @RequestBody NotificationRequestDto notificationRequestDto
    ) {
        userService.updateNotificationSubscribed(user, notificationRequestDto.getSubscribed());
        return SisoResponse.success(null);
    }

    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴를 진행합니다. (소프트 삭제)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "탈퇴 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
            @ApiResponse(responseCode = "404", description = "사용자 없음", content = @Content)
    })
    @DeleteMapping(value = "/delete", produces = "application/json; charset=UTF-8")
    public SisoResponse<Void> deleteUser(
            @Parameter(hidden = true) @CurrentUser User user
    ) {
        userService.deleteUser(user);
        return SisoResponse.success(null);
    }

    @Operation(summary = "로그아웃", description = "로그아웃을 진행합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
    })
    @PostMapping(value = "/logout", produces = "application/json; charset=UTF-8")
    public SisoResponse<Void> logout(
            @Parameter(hidden = true) @CurrentUser User user
    ) {
        userService.logout(user);
        return SisoResponse.success(null);
    }
}
