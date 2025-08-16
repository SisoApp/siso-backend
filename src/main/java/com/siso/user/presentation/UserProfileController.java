package com.siso.user.presentation;

import com.siso.user.application.UserProfileService;
import com.siso.user.dto.UserProfileDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserProfileController {
    private final UserProfileService userProfileService;

    // 단일 조회
    @GetMapping("/{id}")
    public ResponseEntity<UserProfileDto> getProfile(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(userProfileService.findById(id));
    }

    // 전체 조회
    @GetMapping
    public ResponseEntity<List<UserProfileDto>> getAllProfiles() {
        return ResponseEntity.ok(userProfileService.findAll());
    }

    // 프로필 생성
    @PostMapping
    public ResponseEntity<UserProfileDto> createProfile(@RequestBody UserProfileDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userProfileService.create(dto));
    }

    // 프로필 수정
    @PutMapping("/{id}")
    public ResponseEntity<UserProfileDto> updateProfile(@PathVariable(name = "id") Long id,
                                                        @RequestBody UserProfileDto dto) {
        return ResponseEntity.ok(userProfileService.update(id, dto));
    }

    // 프로필 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfile(@PathVariable(name = "id") Long id) {
        userProfileService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
