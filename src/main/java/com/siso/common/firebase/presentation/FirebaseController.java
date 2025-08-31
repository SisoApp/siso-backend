package com.siso.common.firebase.presentation;

import com.siso.common.firebase.application.FirebaseService;
import com.siso.common.firebase.application.FcmTokenService;
import com.siso.common.firebase.dto.FirebaseMessageRequestDto;
import com.siso.common.firebase.dto.FcmTokenRequestDto;
import com.siso.user.domain.model.User;
import com.siso.user.domain.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Firebase Cloud Messaging REST API 컨트롤러
 * 
 * FCM 토큰 관리와 푸시 알림 전송을 위한 REST API 엔드포인트를 제공합니다.
 * 클라이언트 앱(Android/iOS)에서 FCM 토큰을 등록/해제하고,
 * 서버에서 특정 사용자에게 푸시 알림을 전송할 수 있습니다.
 * 
 * @author SISO Team
 * @since 1.0
 */
@RestController
@RequestMapping("/api/fcm")
@RequiredArgsConstructor
@Tag(name = "Firebase Cloud Messaging", description = "FCM 관련 API")
public class FirebaseController {

    private final FirebaseService firebaseService;
    private final FcmTokenService fcmTokenService;
    private final UserRepository userRepository;

    /**
     * FCM 토큰을 등록하거나 업데이트합니다.
     * 
     * 클라이언트 앱에서 FCM 토큰을 획득한 후 이 API를 호출하여 서버에 등록해야 합니다.
     * 동일한 사용자의 동일한 토큰이 이미 존재하는 경우 활성화 상태로 유지됩니다.
     * 
     * @param requestDto FCM 토큰 등록 요청 데이터 (userId, token 포함)
     * @return 등록 성공/실패 메시지
     */
    @PostMapping("/token")
    @Operation(summary = "FCM 토큰 등록", description = "사용자의 FCM 토큰을 등록합니다.")
    public ResponseEntity<String> registerToken(@RequestBody FcmTokenRequestDto requestDto) {
        try {
            // FCM 토큰 저장 또는 업데이트
            fcmTokenService.saveOrUpdateToken(
                requestDto.getUserId(),
                requestDto.getToken()
            );
            return ResponseEntity.ok("Token registered successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to register token: " + e.getMessage());
        }
    }

    /**
     * FCM 토큰을 비활성화합니다.
     * 
     * 사용자가 로그아웃하거나 앱을 삭제할 때 호출되어야 합니다.
     * 토큰을 완전히 삭제하지 않고 비활성화 상태로 변경하여 더 이상 알림을 받지 않게 됩니다.
     * 
     * @param requestDto FCM 토큰 해제 요청 데이터 (userId, token 포함)
     * @return 해제 성공/실패 메시지
     */
    @DeleteMapping("/token")
    @Operation(summary = "FCM 토큰 해제", description = "사용자의 FCM 토큰을 비활성화합니다.")
    public ResponseEntity<String> unregisterToken(@RequestBody FcmTokenRequestDto requestDto) {
        try {
            // FCM 토큰 비활성화
            fcmTokenService.deactivateToken(requestDto.getUserId(), requestDto.getToken());
            return ResponseEntity.ok("Token unregistered successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to unregister token: " + e.getMessage());
        }
    }

    /**
     * 특정 사용자에게 푸시 알림을 전송합니다.
     * 
     * 해당 사용자의 모든 활성화된 디바이스(FCM 토큰)에 푸시 알림을 전송합니다.
     * 사용자가 여러 디바이스를 사용하는 경우 모든 디바이스에 알림이 전송됩니다.
     * 
     * @param requestDto 메시지 전송 요청 데이터 (userId, title, body 포함)
     * @return 전송 성공/실패 메시지 및 전송된 디바이스 수
     */
    @PostMapping("/sendMessage")
    @Operation(summary = "메시지 전송", description = "특정 사용자에게 푸시 알림을 전송합니다.")
    public ResponseEntity<String> sendMessage(@RequestBody FirebaseMessageRequestDto requestDto) {
        try {
            Long userId = Long.valueOf(requestDto.getUserId());
            
            // 사용자의 알림 구독 상태 확인
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("유저의 아이디를 찾을수 없습니다: " + userId));
            
            if (!user.isNotificationSubscribed()) {
                return ResponseEntity.badRequest().body("유저가 알림을 차단하였습니다: " + requestDto.getUserId());
            }
            
            // userId로 활성화된 FCM 토큰들을 조회
            List<String> tokens = fcmTokenService.getActiveTokensByUserId(userId);
            
            // 활성화된 토큰이 없는 경우 에러 응답
            if (tokens.isEmpty()) {
                return ResponseEntity.badRequest().body("유효한 FCM 토큰이 없습니다: " + requestDto.getUserId());
            }

            // FCM을 통해 멀티캐스트 메시지 전송
            firebaseService.sendMulticast(
                tokens,
                requestDto.getTitle(),
                requestDto.getBody(),
                "", // URL - 딥링크가 필요한 경우 추가
                "MESSAGE", // 알림 타입
                System.currentTimeMillis() + "" // 알림 고유 ID
            );
            return ResponseEntity.ok("Message sent successfully to " + tokens.size() + " devices");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to send message: " + e.getMessage());
        }
    }
}