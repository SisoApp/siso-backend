package com.siso.common.util;

import net.logstash.logback.marker.Markers;
import org.slf4j.Marker;

import java.util.HashMap;
import java.util.Map;

/**
 * 로깅 유틸리티
 * - 구조화된 로깅을 위한 Key-Value 헬퍼 메서드
 * - JSON 로그에서 쉽게 검색 가능한 필드 제공
 */
public class LogUtil {

    /**
     * Key-Value 쌍 생성
     *
     * 사용 예시:
     * log.info("User login",
     *     kv("userId", user.getId()),
     *     kv("email", user.getEmail())
     * );
     */
    public static Marker kv(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return Markers.append(key, value);
    }

    /**
     * 여러 Key-Value 쌍을 한 번에 생성
     *
     * 사용 예시:
     * log.info("API call",
     *     kvs(
     *         "userId", user.getId(),
     *         "action", "call_request",
     *         "duration", duration
     *     )
     * );
     */
    public static Marker kvs(Object... keyValues) {
        if (keyValues.length % 2 != 0) {
            throw new IllegalArgumentException("Key-Value pairs must be even number of arguments");
        }

        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            String key = keyValues[i].toString();
            Object value = keyValues[i + 1];
            map.put(key, value);
        }

        return Markers.appendEntries(map);
    }

    /**
     * 사용자 액션 로깅용 마커
     */
    public static Marker userAction(Long userId, String action) {
        Map<String, Object> map = new HashMap<>();
        map.put("userId", userId);
        map.put("action", action);
        map.put("type", "user_action");
        return Markers.appendEntries(map);
    }

    /**
     * 에러 로깅용 마커 (추가 컨텍스트 포함)
     */
    public static Marker error(String errorCode, String errorMessage, Object context) {
        Map<String, Object> map = new HashMap<>();
        map.put("errorCode", errorCode);
        map.put("errorMessage", errorMessage);
        map.put("context", context);
        map.put("type", "error");
        return Markers.appendEntries(map);
    }

    /**
     * 성능 측정용 마커
     */
    public static Marker performance(String operation, long durationMs) {
        Map<String, Object> map = new HashMap<>();
        map.put("operation", operation);
        map.put("durationMs", durationMs);
        map.put("type", "performance");
        return Markers.appendEntries(map);
    }
}
