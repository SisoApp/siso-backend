package com.siso.common.firebase.infrastructure;

import com.google.firebase.messaging.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * FCM 멀티캐스트 전송 헬퍼
 * - 배치 단위로 sendEachForMulticast 호출
 * - 성공/실패/무효 토큰 결과 수집
 */
@Slf4j
@Component
public class MulticastResultConfig {

    // ===== 결과 DTO =====
    @Data
    @Builder
    @AllArgsConstructor
    public static class SendMulticastResult {
        private int requestedCount;               // 요청한 총 토큰 수
        private int successCount;                 // 성공 개수
        private int failureCount;                 // 실패 개수
        private List<String> invalidTokens;       // 만료/무효 토큰 (정리 권장)
        private Map<String, String> tokenErrors;  // token -> error message
    }

    /**
     * 멀티캐스트 전송(배치) 후 결과를 요약해서 반환합니다.
     */
    public SendMulticastResult sendMulticastResult(Collection<String> tokens,
                                                   String title,
                                                   String body,
                                                   Map<String, String> data,
                                                   int batchSize,
                                                   boolean dryRun) {

        if (tokens == null || tokens.isEmpty()) {
            return SendMulticastResult.builder()
                    .requestedCount(0)
                    .successCount(0)
                    .failureCount(0)
                    .invalidTokens(Collections.emptyList())
                    .tokenErrors(Collections.emptyMap())
                    .build();
        }

        List<List<String>> batches = splitIntoBatches(new ArrayList<>(tokens), Math.max(1, batchSize));

        int totalSuccess = 0;
        int totalFailure = 0;
        Map<String, String> tokenErrors = new LinkedHashMap<>();
        List<String> invalidTokens = new ArrayList<>();

        for (List<String> batch : batches) {
            MulticastMessage.Builder builder = MulticastMessage.builder()
                    .addAllTokens(batch)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build());

            if (data != null && !data.isEmpty()) {
                data.forEach(builder::putData);
            }

            try {
                BatchResponse response = FirebaseMessaging.getInstance()
                        .sendEachForMulticast(builder.build(), dryRun);

                totalSuccess += response.getSuccessCount();
                totalFailure += response.getFailureCount();

                List<SendResponse> responses = response.getResponses();
                for (int i = 0; i < responses.size(); i++) {
                    SendResponse r = responses.get(i);
                    String token = batch.get(i);

                    if (!r.isSuccessful()) {
                        String msg = (r.getException() != null)
                                ? r.getException().getMessage()
                                : "Unknown error";
                        tokenErrors.put(token, msg);
                        if (isInvalidToken(r)) invalidTokens.add(token);
                    }
                }
            } catch (FirebaseMessagingException e) {
                for (String token : batch) tokenErrors.put(token, e.getMessage());
                totalFailure += batch.size();
                log.error("FCM batch send failed: {}", e.getMessage(), e);
            }
        }

        SendMulticastResult result = SendMulticastResult.builder()
                .requestedCount(tokens.size())
                .successCount(totalSuccess)
                .failureCount(totalFailure)
                .invalidTokens(invalidTokens)
                .tokenErrors(tokenErrors)
                .build();

        log.info("[FCM] requested={}, success={}, failure={}, invalidTokens={}",
                result.getRequestedCount(), result.getSuccessCount(),
                result.getFailureCount(), result.getInvalidTokens().size());

        return result;
    }

    // ===== 유틸 =====
    private static List<List<String>> splitIntoBatches(List<String> tokens, int batchSize) {
        List<List<String>> out = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i += batchSize) {
            out.add(tokens.subList(i, Math.min(i + batchSize, tokens.size())));
        }
        return out;
    }

    /** 만료/무효 토큰 판별 */
    private static boolean isInvalidToken(SendResponse r) {
        if (r == null || r.isSuccessful() || r.getException() == null) return false;
        MessagingErrorCode code = r.getException().getMessagingErrorCode();
        if (code == null) return false;
        switch (code) {
            case UNREGISTERED:      // 등록 취소/만료
            case INVALID_ARGUMENT:  // 형식 오류
            case SENDER_ID_MISMATCH:
                return true;
            default:
                return false;
        }
    }
}