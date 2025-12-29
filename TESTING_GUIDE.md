# SISO 프로젝트 테스트 가이드

## 통합 테스트 구성 방법

### 1. 필요한 의존성 추가

`build.gradle`에 다음 의존성을 추가하세요:

```gradle
dependencies {
    // 기존 의존성...

    // 테스트 프레임워크
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'

    // Testcontainers (실제 MySQL 컨테이너로 테스트)
    testImplementation 'org.testcontainers:testcontainers:1.19.3'
    testImplementation 'org.testcontainers:junit-jupiter:1.19.3'
    testImplementation 'org.testcontainers:mysql:1.19.3'

    // WebSocket 테스트
    testImplementation 'org.springframework.boot:spring-boot-starter-websocket'

    // AssertJ (유창한 assertion)
    testImplementation 'org.assertj:assertj-core:3.24.2'

    // MockWebServer (외부 API 모킹 - OAuth, FCM 등)
    testImplementation 'com.squareup.okhttp3:mockwebserver:4.12.0'

    // Awaitility (비동기 테스트)
    testImplementation 'org.awaitility:awaitility:4.2.0'
}
```

### 2. 통합 테스트 기본 설정 클래스

`src/test/java/com/siso/config/IntegrationTestBase.java`:

```java
package com.siso.config;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@Transactional  // 각 테스트 후 롤백
public abstract class IntegrationTestBase {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("siso_test")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    protected MockMvc mockMvc;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        // Testcontainers MySQL 설정
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);

        // 테스트용 설정
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");

        // AWS S3 비활성화 (Mock 사용)
        registry.add("cloud.aws.stack.auto", () -> "false");
        registry.add("cloud.aws.region.static", () -> "ap-northeast-2");

        // FCM 비활성화 (Mock 사용)
        registry.add("fcm.enabled", () -> "false");
    }

    @BeforeEach
    void setUp() {
        // 각 테스트 전에 공통 설정
    }
}
```

### 3. 통합 테스트 예시

#### 3.1 Repository 통합 테스트

`src/test/java/com/siso/user/domain/repository/UserRepositoryIntegrationTest.java`:

```java
package com.siso.user.domain.repository;

import com.siso.config.IntegrationTestBase;
import com.siso.user.domain.model.User;
import com.siso.user.domain.model.UserProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserRepositoryIntegrationTest extends IntegrationTestBase {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("30일 지난 soft delete 사용자 조회")
    void findUsersForHardDelete_shouldReturnUsersOlderThan30Days() {
        // Given: 35일 전에 삭제된 사용자 생성
        User user = User.builder()
                .email("test@example.com")
                .provider("KAKAO")
                .providerId("12345")
                .isDeleted(true)
                .deletedAt(LocalDateTime.now().minusDays(35))
                .build();
        userRepository.save(user);

        // When: 30일 지난 사용자 조회
        List<User> users = userRepository.findUsersForHardDelete(
                LocalDateTime.now().minusDays(30)
        );

        // Then
        assertThat(users).hasSize(1);
        assertThat(users.get(0).getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("N+1 문제 없이 사용자와 이미지, 프로필 조회")
    void findByIdWithImagesAndProfile_shouldFetchInOneQuery() {
        // Given: 사용자, 프로필, 이미지 생성
        User user = User.builder()
                .email("test@example.com")
                .provider("KAKAO")
                .providerId("12345")
                .build();

        UserProfile profile = UserProfile.builder()
                .user(user)
                .nickname("테스터")
                .age(25)
                .build();

        user.setProfile(profile);
        userRepository.save(user);

        // When: JOIN FETCH로 조회
        User found = userRepository.findByIdWithImagesAndProfile(user.getId())
                .orElseThrow();

        // Then: LazyInitializationException 없이 접근 가능
        assertThat(found.getProfile().getNickname()).isEqualTo("테스터");
        assertThat(found.getImages()).isNotNull();
    }
}
```

#### 3.2 Service 통합 테스트 (외부 서비스 Mock)

`src/test/java/com/siso/call/application/AgoraCallServiceIntegrationTest.java`:

```java
package com.siso.call.application;

import com.siso.call.domain.model.Call;
import com.siso.call.domain.repository.CallRepository;
import com.siso.config.IntegrationTestBase;
import com.siso.user.domain.model.User;
import com.siso.user.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class AgoraCallServiceIntegrationTest extends IntegrationTestBase {

    @Autowired
    private AgoraCallService agoraCallService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CallRepository callRepository;

    // 외부 서비스는 Mock
    @MockBean
    private AgoraTokenService agoraTokenService;

    @MockBean
    private NotificationService notificationService;

    @Test
    @DisplayName("통화 요청 시 수신자가 IN_CALL 상태면 예외 발생")
    void requestCall_whenReceiverInCall_shouldThrowException() {
        // Given: 발신자, 수신자 생성
        User caller = createUser("caller@test.com");
        User receiver = createUser("receiver@test.com");

        // 수신자를 IN_CALL 상태로 변경
        receiver.updatePresenceStatus(PresenceStatus.IN_CALL);
        userRepository.save(receiver);

        // Mock Agora 토큰 생성
        when(agoraTokenService.generateToken(anyString(), anyString()))
                .thenReturn("mock-agora-token");

        // When & Then: 예외 발생 확인
        assertThatThrownBy(() ->
                agoraCallService.requestCall(caller.getId(), receiver.getId())
        )
        .isInstanceOf(ExpectedException.class)
        .hasMessageContaining("이미 통화 중");
    }

    @Test
    @DisplayName("통화 수락 시 두 사용자 모두 IN_CALL 상태로 변경")
    void acceptCall_shouldUpdateBothUsersToInCall() {
        // Given
        User caller = createUser("caller@test.com");
        User receiver = createUser("receiver@test.com");

        when(agoraTokenService.generateToken(anyString(), anyString()))
                .thenReturn("mock-token");

        Call call = agoraCallService.requestCall(caller.getId(), receiver.getId());

        // When: 통화 수락
        agoraCallService.acceptCall(call.getId(), receiver.getId());

        // Then: 두 사용자 모두 IN_CALL 상태
        User updatedCaller = userRepository.findById(caller.getId()).orElseThrow();
        User updatedReceiver = userRepository.findById(receiver.getId()).orElseThrow();

        assertThat(updatedCaller.getPresenceStatus()).isEqualTo(PresenceStatus.IN_CALL);
        assertThat(updatedReceiver.getPresenceStatus()).isEqualTo(PresenceStatus.IN_CALL);
    }

    private User createUser(String email) {
        User user = User.builder()
                .email(email)
                .provider("KAKAO")
                .providerId(email)
                .build();
        return userRepository.save(user);
    }
}
```

#### 3.3 Controller 통합 테스트 (API 엔드포인트)

`src/test/java/com/siso/user/presentation/UserControllerIntegrationTest.java`:

```java
package com.siso.user.presentation;

import com.siso.config.IntegrationTestBase;
import com.siso.user.domain.model.User;
import com.siso.user.domain.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UserControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("GET /api/users/{userId} - 사용자 조회 성공")
    @WithMockUser(username = "1")  // Mock 인증
    void getUser_shouldReturnUserDetails() throws Exception {
        // Given: 사용자 생성
        User user = User.builder()
                .email("test@example.com")
                .provider("KAKAO")
                .providerId("12345")
                .build();
        user = userRepository.save(user);

        // When & Then: API 호출 및 검증
        mockMvc.perform(get("/api/users/{userId}", user.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("test@example.com")))
                .andExpect(jsonPath("$.provider", is("KAKAO")));
    }

    @Test
    @DisplayName("DELETE /api/users/{userId} - 사용자 soft delete 성공")
    @WithMockUser(username = "1")
    void deleteUser_shouldSoftDelete() throws Exception {
        // Given
        User user = User.builder()
                .email("test@example.com")
                .provider("KAKAO")
                .providerId("12345")
                .build();
        user = userRepository.save(user);

        // When: DELETE 요청
        mockMvc.perform(delete("/api/users/{userId}", user.getId()))
                .andExpect(status().isNoContent());

        // Then: isDeleted = true 확인
        User deletedUser = userRepository.findById(user.getId()).orElseThrow();
        assertThat(deletedUser.getIsDeleted()).isTrue();
        assertThat(deletedUser.getDeletedAt()).isNotNull();
    }
}
```

#### 3.4 파일 업로드 통합 테스트 (S3 Mock)

`src/test/java/com/siso/voicesample/application/VoiceSampleServiceIntegrationTest.java`:

```java
package com.siso.voicesample.application;

import com.siso.config.IntegrationTestBase;
import com.siso.common.util.S3UploadUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class VoiceSampleServiceIntegrationTest extends IntegrationTestBase {

    @Autowired
    private VoiceSampleService voiceSampleService;

    @MockBean
    private S3UploadUtil s3UploadUtil;

    @Test
    @DisplayName("음성 파일 20초 초과 시 예외 발생")
    void uploadVoiceSample_whenDurationExceeds20s_shouldThrowException() {
        // Given: 25초 길이의 Mock 음성 파일 (실제로는 메타데이터 조작 필요)
        MockMultipartFile file = new MockMultipartFile(
                "voice",
                "sample.mp3",
                "audio/mpeg",
                "fake audio content".getBytes()
        );

        when(s3UploadUtil.upload(any(), any())).thenReturn("s3-url");

        // When & Then: 20초 초과 예외
        // 실제 구현에서는 JAVE/MP3AGIC로 길이 추출
        assertThatThrownBy(() ->
                voiceSampleService.uploadVoiceSample(1L, file)
        )
        .isInstanceOf(ExpectedException.class)
        .hasMessageContaining("20초");
    }
}
```

### 4. 외부 서비스 Mock 전략

#### 4.1 AWS S3 Mock

`src/test/java/com/siso/config/S3MockConfig.java`:

```java
@TestConfiguration
public class S3MockConfig {

    @Bean
    @Primary
    public S3UploadUtil mockS3UploadUtil() {
        S3UploadUtil mock = Mockito.mock(S3UploadUtil.class);

        when(mock.upload(any(), any()))
                .thenReturn("https://mock-s3.amazonaws.com/test.jpg");

        when(mock.generatePresignedUrl(any()))
                .thenReturn("https://mock-s3.amazonaws.com/presigned-url");

        return mock;
    }
}
```

#### 4.2 Firebase FCM Mock

`src/test/java/com/siso/config/FcmMockConfig.java`:

```java
@TestConfiguration
public class FcmMockConfig {

    @Bean
    @Primary
    public FirebaseMessaging mockFirebaseMessaging() {
        return Mockito.mock(FirebaseMessaging.class);
    }
}
```

#### 4.3 OAuth Provider Mock (MockWebServer 사용)

`src/test/java/com/siso/user/infrastructure/KakaoOAuthProviderTest.java`:

```java
class KakaoOAuthProviderTest {

    private MockWebServer mockWebServer;
    private KakaoOAuthProvider kakaoOAuthProvider;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        String baseUrl = mockWebServer.url("/").toString();
        kakaoOAuthProvider = new KakaoOAuthProvider(baseUrl);
    }

    @Test
    @DisplayName("카카오 사용자 정보 조회 성공")
    void getUserInfo_shouldReturnUserInfo() {
        // Mock 응답 설정
        mockWebServer.enqueue(new MockResponse()
                .setBody("{\"id\":\"12345\",\"kakao_account\":{\"email\":\"test@kakao.com\"}}")
                .addHeader("Content-Type", "application/json"));

        // When
        OAuthUserInfo userInfo = kakaoOAuthProvider.getUserInfo("mock-access-token");

        // Then
        assertThat(userInfo.getProviderId()).isEqualTo("12345");
        assertThat(userInfo.getEmail()).isEqualTo("test@kakao.com");
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }
}
```

### 5. WebSocket 통합 테스트

`src/test/java/com/siso/chat/presentation/ChatWebSocketTest.java`:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChatWebSocketTest {

    @LocalServerPort
    private int port;

    private StompSession stompSession;

    @BeforeEach
    void setUp() throws Exception {
        WebSocketStompClient stompClient = new WebSocketStompClient(
                new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient())))
        );
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());

        String url = "ws://localhost:" + port + "/ws";
        stompSession = stompClient.connectAsync(url, new StompSessionHandlerAdapter() {})
                .get(5, TimeUnit.SECONDS);
    }

    @Test
    @DisplayName("채팅 메시지 전송 및 수신")
    void sendMessage_shouldReceiveMessage() throws Exception {
        // Given
        CompletableFuture<ChatMessage> future = new CompletableFuture<>();

        stompSession.subscribe("/topic/chat/1", new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return ChatMessage.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                future.complete((ChatMessage) payload);
            }
        });

        // When: 메시지 전송
        ChatMessageRequest request = new ChatMessageRequest("안녕하세요");
        stompSession.send("/app/chat/1/send", request);

        // Then: 메시지 수신 확인
        ChatMessage received = future.get(5, TimeUnit.SECONDS);
        assertThat(received.getContent()).isEqualTo("안녕하세요");
    }
}
```

### 6. 테스트 실행 명령어

```bash
# 모든 테스트 실행
./gradlew test

# 통합 테스트만 실행
./gradlew test --tests "*IntegrationTest"

# 특정 테스트 클래스 실행
./gradlew test --tests "UserRepositoryIntegrationTest"

# 테스트 커버리지 확인
./gradlew test jacocoTestReport
```

### 7. 테스트 작성 우선순위

1. **최우선**: 인증, 통화 관리, 채팅 제한
2. **높음**: 파일 업로드, 사용자 매칭, 삭제 로직
3. **중간**: 알림, Repository 커스텀 쿼리
4. **낮음**: 단순 CRUD, Getter/Setter

---

## 단위 테스트 vs 통합 테스트 선택 기준

| 테스트 대상 | 단위 테스트 | 통합 테스트 |
|------------|-----------|-----------|
| **Service 비즈니스 로직** | ✅ (외부 의존성 Mock) | ✅ (전체 플로우 검증) |
| **Repository 쿼리** | ❌ | ✅ (실제 DB 필요) |
| **Controller API** | ✅ (MockMvc) | ✅ (전체 스택) |
| **외부 API 호출** | ✅ (MockWebServer) | ✅ (실제 환경에서) |
| **복잡한 트랜잭션** | ❌ | ✅ |
| **WebSocket** | ❌ | ✅ |

---

## 추가 참고사항

- **Testcontainers**는 Docker를 사용하므로 Docker가 설치되어 있어야 합니다
- 통합 테스트는 단위 테스트보다 느리므로, CI/CD에서는 병렬 실행을 고려하세요
- 민감한 정보(API Key 등)는 테스트 환경 변수로 관리하세요
- 테스트 데이터는 `@Transactional`로 자동 롤백되지만, Testcontainers는 테스트 종료 시 자동 삭제됩니다
