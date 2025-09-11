package com.siso.user.presentation;

import com.siso.common.web.CurrentUser;
import com.siso.user.application.UserFilterService;
import com.siso.user.domain.model.User;
import com.siso.user.dto.response.MatchingProfileResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 사용자 필터링 및 매칭 관련 API 컨트롤러
 * 
 * 무한 스크롤 매칭 기능을 담당합니다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/filter")
public class UserFilterController {
    private final UserFilterService userFilterService;

    /**
     * 매칭용 프로필 조회 (무한 스크롤 지원)
     * 
     * @param user 현재 사용자
     * @param page 페이지 번호 (0부터 시작, 기본값: 0)
     * @param count 조회할 프로필 개수 (기본값: 5)
     * @return 매칭용 프로필 리스트
     */
    @GetMapping(value = "/matching", produces = "application/json; charset=UTF-8")
    public ResponseEntity<List<MatchingProfileResponseDto>> getMatchingProfiles(
            @CurrentUser User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int count) {
        
        List<MatchingProfileResponseDto> profiles = userFilterService.getMatchingProfiles(user, page, count);
        return ResponseEntity.ok(profiles);
    }
}
