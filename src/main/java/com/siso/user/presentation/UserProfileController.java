package com.siso.user.presentation;

import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.common.web.CurrentUser;
import com.siso.user.application.UserProfileService;
import com.siso.user.domain.model.User;
import com.siso.user.dto.request.UserProfileRequestDto;
import com.siso.user.dto.response.UserProfileResponseDto;
import com.siso.image.dto.response.ImageResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/profiles")
public class UserProfileController {
    private final UserProfileService userProfileService;

    // 단일 조회
    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponseDto> getProfile(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(userProfileService.findById(id));
    }

    // 전체 조회
    @GetMapping
    public ResponseEntity<List<UserProfileResponseDto>> getAllProfiles() {
        return ResponseEntity.ok(userProfileService.findAll());
    }

    // 사용자 기준 프로필 조회
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponseDto> getProfileByUser(@CurrentUser User user) {
        UserProfileResponseDto profile = userProfileService.getUserProfileByUserId(user.getId());
        return ResponseEntity.ok(profile);
    }

    // 프로필 생성
    @PostMapping
    public ResponseEntity<UserProfileResponseDto> createProfile(@CurrentUser User user,
                                                                @Valid @RequestBody UserProfileRequestDto dto) {
        if (user == null) { // 인증 실패는 401로
            throw new ExpectedException(ErrorCode.UNAUTHROIZED);
        }

        System.out.println("========================= user: " + user);
        return ResponseEntity.status(HttpStatus.CREATED).body(userProfileService.create(user, dto));
    }

    // 프로필 수정
    @PatchMapping
    public ResponseEntity<UserProfileResponseDto> updateProfile(@CurrentUser User user,
                                                                @Valid @RequestBody UserProfileRequestDto dto) {
        return ResponseEntity.ok(userProfileService.update(user, dto));
    }

    // 프로필 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfile(@PathVariable(name = "id") Long id) {
        userProfileService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // 사용자의 모든 이미지 조회 (프로필 이미지 설정용)
    @GetMapping("/images")
    public ResponseEntity<List<ImageResponseDto>> getProfileImages(@CurrentUser User user) {
        List<ImageResponseDto> images = userProfileService.getUserImages(user.getId());
        return ResponseEntity.ok(images);
    }
}