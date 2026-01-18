# SISO 프로젝트 테스트 가이드

## 📚 목차
1. [통합 테스트 구성 방법](#통합-테스트-구성-방법)
2. [단위 테스트 작성법](#단위-테스트-작성법)
3. [통합 테스트 작성법](#통합-테스트-작성법)
4. [예외 처리 테스트](#예외-처리-테스트)
5. [성능 및 동시성 테스트](#성능-및-동시성-테스트)
6. [E2E 테스트](#e2e-테스트)
7. [테스트 실행 방법](#테스트-실행-방법)

---

## 통합 테스트 구성 방법

### 1. 필요한 의존성 추가

`build.gradle`에 다음 의존성을 추가하세요:

```gradle
dependencies {
    // 기존 의존성...

    // 테스트 프레임워크
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'

    // Testcontainers (실제 MySQL 컨테이너로 테스트)
    testImplementation 'org.testcontainers:testcontainers:1.19.3'
    testImplementation 'org.testcontainers:junit-jupiter:1.19.3'
    testImplementation 'org.testcontainers:mysql:1.19.3'
    testImplementation 'org.testcontainers:rabbitmq:1.19.3'

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

---

## 단위 테스트 작성법

### 1. Service 단위 테스트 예시

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("사용자 조회 성공")
    void getUserById_shouldReturnUser() {
        // Given
        User mockUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        // When
        User result = userService.getUserById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(userRepository, times(1)).findById(1L);
    }
}
```

---

## 통합 테스트 작성법

### 1. Repository 통합 테스트

```java
@DisplayName("UserRepository 통합 테스트")
class UserRepositoryIntegrationTest extends IntegrationTestBase {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("이메일로 사용자 조회")
    void findByEmail_shouldReturnUser() {
        // Given
        User user = User.builder()
                .email("test@example.com")
                .provider(Provider.KAKAO)
                .providerId("12345")
                .build();
        userRepository.save(user);

        // When
        Optional<User> found = userRepository.findByEmail("test@example.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");
    }
}
```

### 2. Controller 통합 테스트 (MockMvc)

```java
@DisplayName("UserController 통합 테스트")
class UserControllerIntegrationTest extends IntegrationTestBase {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Test
    @DisplayName("GET /api/users/info - 사용자 조회 성공")
    void getUserInfo_shouldReturnUserDetails() throws Exception {
        // Given
        User user = User.builder()
                .email("test@example.com")
                .provider(Provider.KAKAO)
                .phoneNumber("010-1234-5678")
                .presenceStatus(PresenceStatus.ONLINE)
                .build();
        user = userRepository.save(user);

        String accessToken = jwtTokenUtil.generateAccessToken(user.getEmail());

        // When & Then
        mockMvc.perform(get("/api/users/info")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.provider").value("KAKAO"));
    }
}
```

---

## 예외 처리 테스트

### 1. JWT 예외 테스트

```java
@DisplayName("JWT 인증 예외 테스트")
class JwtAuthenticationExceptionTest extends IntegrationTestBase {

    @Test
    @DisplayName("토큰 없이 요청 시 401 Unauthorized")
    void whenNoToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/users/info")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("만료된 토큰으로 요청 시 401 + TOKEN_EXPIRED 에러")
    void whenExpiredToken_shouldReturn401WithError() throws Exception {
        // Given: 만료된 토큰 생성
        Date pastDate = new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24);
        Date expiredDate = new Date(pastDate.getTime() + 1000);

        String expiredToken = Jwts.builder()
                .setSubject("test@example.com")
                .claim("type", "access")
                .setIssuedAt(pastDate)
                .setExpiration(expiredDate)
                .signWith(Keys.hmacShaKeyFor(getSecretKey().getBytes()), SignatureAlgorithm.HS256)
                .compact();

        // When & Then
        mockMvc.perform(get("/api/users/info")
                .header("Authorization", "Bearer " + expiredToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
```

### 2. 입력 검증 테스트

```java
@DisplayName("입력 검증 테스트")
class InputValidationTest extends IntegrationTestBase {

    @Test
    @DisplayName("나이가 19세 미만이면 400 에러")
    void whenAgeTooYoung_shouldReturn400() throws Exception {
        // Given: 나이 18세
        UserProfileRequestDto invalidDto = new UserProfileRequestDto(
                null, null, false, 18, "테스터", "안녕하세요", "서울시 강남구",
                Sex.MALE, PreferenceSex.FEMALE, null,
                List.of(Meeting.FRIENDSHIP, Meeting.DATE, Meeting.CHAT)
        );

        // When & Then
        mockMvc.perform(post("/api/profiles")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[?(@.field == 'age')].message")
                        .value("나이는 최소 19세 이상이어야 합니다."));
    }
}
```

---

## 성능 및 동시성 테스트

### 1. 동시성 테스트

```java
@DisplayName("동시성 테스트")
class ConcurrencyTest extends IntegrationTestBase {

    @Test
    @DisplayName("100개의 메시지를 동시에 전송해도 모두 저장")
    void whenConcurrentMessageSending_allMessagesShouldBeSaved() throws InterruptedException {
        // Given
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        // When: 동시에 메시지 전송
        for (int i = 0; i < threadCount; i++) {
            final int messageNum = i;
            executorService.submit(() -> {
                try {
                    ChatMessage message = ChatMessage.builder()
                            .chatRoom(chatRoom)
                            .sender(messageNum % 2 == 0 ? user1 : user2)
                            .message("메시지 " + messageNum)
                            .build();
                    chatMessageRepository.save(message);
                    successCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(10, TimeUnit.SECONDS);
        executorService.shutdown();

        // Then: 모든 메시지가 저장되었는지 확인
        assertThat(successCount.get()).isEqualTo(threadCount);
        List<ChatMessage> savedMessages = chatMessageRepository.findAll();
        assertThat(savedMessages).hasSize(threadCount);
    }
}
```

### 2. 성능 테스트

```java
@DisplayName("AI 매칭 성능 테스트")
class MatchingAlgorithmPerformanceTest extends IntegrationTestBase {

    @Test
    @DisplayName("1000명 후보 대상 매칭이 150ms 이내에 완료")
    void whenMatching1000Candidates_shouldCompleteUnder150ms() {
        // Given: 1000명의 후보 사용자 생성
        List<Interest> interests = interestRepository.findAll();
        createCandidateUsers(1000, interests);

        // When: 매칭 알고리즘 실행
        long startTime = System.currentTimeMillis();
        MatchingResultDto result = matchingAlgorithmService.calculateMatches(targetUser);
        long executionTime = System.currentTimeMillis() - startTime;

        // Then: 150ms 이내에 완료
        assertThat(executionTime).isLessThan(150L);
        assertThat(result.getMatches()).isNotEmpty();
        assertThat(result.getMatches()).hasSizeLessThanOrEqualTo(20);

        System.out.println("실행 시간: " + executionTime + "ms");
    }
}
```

---

## E2E 테스트

### 1. 사용자 전체 플로우 E2E 테스트

E2E(End-to-End) 테스트는 실제 사용자 시나리오를 전체 플로우로 검증합니다.

```java
@DisplayName("사용자 전체 플로우 E2E 테스트")
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserJourneyE2ETest extends IntegrationTestBase {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("E2E: 회원가입 → 프로필 생성 → AI 매칭 → 매칭 결과 조회")
    void completeUserMatchingJourney() throws Exception {
        // 1. 사용자 생성 (회원가입)
        User user = User.builder()
                .provider(Provider.KAKAO)
                .email("user@example.com")
                .phoneNumber("010-1234-5678")
                .presenceStatus(PresenceStatus.ONLINE)
                .registrationStatus(RegistrationStatus.LOGIN)
                .build();
        user = userRepository.save(user);

        String accessToken = jwtTokenUtil.generateAccessToken(user.getEmail());

        // 2. 프로필 생성
        UserProfileRequestDto profileDto = new UserProfileRequestDto(
                DrinkingCapacity.MODERATE, Religion.NONE, false, 25, "테스터",
                "안녕하세요", "서울시 강남구",
                Sex.MALE, PreferenceSex.FEMALE, Mbti.ENFP,
                List.of(Meeting.FRIENDSHIP, Meeting.DATE, Meeting.CHAT)
        );

        mockMvc.perform(post("/api/profiles")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(profileDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nickname").value("테스터"));

        // 3. AI 매칭 요청
        MvcResult matchingResult = mockMvc.perform(post("/api/matching/request")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String matchingResponse = matchingResult.getResponse().getContentAsString();
        String requestId = JsonPath.read(matchingResponse, "$.requestId");

        // 4. 매칭 결과 조회 (비동기 처리 대기)
        Thread.sleep(2000);  // 실제로는 폴링이나 WebSocket 사용

        mockMvc.perform(get("/api/matching/results")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andExpect(jsonPath("$.matches").isArray());
    }

    @Test
    @DisplayName("E2E: 통화 → 통화 품질 메트릭 제출 → 통화 리뷰 작성")
    void completeCallReviewJourney() throws Exception {
        // 1. 사용자 및 통화 설정
        User caller = createUser("caller@example.com");
        User receiver = createUser("receiver@example.com");

        String callerToken = jwtTokenUtil.generateAccessToken(caller.getEmail());
        String receiverToken = jwtTokenUtil.generateAccessToken(receiver.getEmail());

        // 2. 통화 요청
        CallRequestDto callRequest = new CallRequestDto(receiver.getId());

        MvcResult callResult = mockMvc.perform(post("/api/calls/request")
                .header("Authorization", "Bearer " + callerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(callRequest)))
                .andExpect(status().isOk())
                .andReturn();

        Long callId = JsonPath.read(callResult.getResponse().getContentAsString(), "$.id");

        // 3. 통화 수락
        mockMvc.perform(post("/api/calls/" + callId + "/accept")
                .header("Authorization", "Bearer " + receiverToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // 4. 통화 종료
        mockMvc.perform(post("/api/calls/" + callId + "/end")
                .header("Authorization", "Bearer " + callerToken)
                .param("createChatRoom", "true")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // 5. 통화 품질 메트릭 제출
        CallQualityMetricsRequestDto qualityDto = new CallQualityMetricsRequestDto(
                callId, 2, 50, 120, 128, 512, "opus", "VP8"
        );

        mockMvc.perform(post("/api/call-quality/metrics")
                .header("Authorization", "Bearer " + callerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(qualityDto)))
                .andExpect(status().isOk());

        // 6. 통화 리뷰 작성
        CallReviewRequestDto reviewDto = new CallReviewRequestDto(
                null, callId, 5, "아주 좋았습니다!"
        );

        mockMvc.perform(post("/api/call-reviews")
                .header("Authorization", "Bearer " + callerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reviewDto)))
                .andExpect(status().isOk());

        // Then: 전체 플로우 성공 확인
        mockMvc.perform(get("/api/call-reviews/call/" + callId)
                .header("Authorization", "Bearer " + callerToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("아주 좋았습니다!"));
    }
}
```

### E2E 테스트 작성 시 주의사항

1. **비동기 처리**: 메시지 큐, 캐시 등 비동기 처리는 충분한 대기 시간 필요
2. **외부 서비스 Mock**: AWS S3, FCM, Agora 등은 Mock 사용
3. **데이터 정합성**: 각 단계마다 DB 상태 검증
4. **트랜잭션 롤백**: `@Transactional`로 테스트 후 자동 롤백

---

## 테스트 실행 방법

### 1. 모든 테스트 실행
```bash
./gradlew test
```

### 2. 특정 카테고리만 실행
```bash
# 통합 테스트만 실행
./gradlew test --tests "*IntegrationTest"

# 단위 테스트만 실행
./gradlew test --tests "*Test" --exclude-task "*IntegrationTest"

# E2E 테스트만 실행
./gradlew test --tests "*E2ETest"

# 성능 테스트만 실행
./gradlew test --tests "*PerformanceTest"

# 동시성 테스트만 실행
./gradlew test --tests "*ConcurrencyTest"

# JWT 예외 테스트만 실행
./gradlew test --tests "JwtAuthenticationExceptionTest"
```

### 3. 테스트 커버리지 확인
```bash
./gradlew test jacocoTestReport

# 리포트 위치: build/reports/jacoco/test/html/index.html
```

### 4. 특정 테스트 메서드만 실행
```bash
./gradlew test --tests "UserRepositoryIntegrationTest.findByEmail_shouldReturnUser"
```

---

## 테스트 작성 우선순위

### 최우선
1. **인증/보안** (JWT, OAuth)
2. **통화 관리** (중복 방지, 상태 전환)
3. **채팅 메시지 제한** (LIMITED/UNLIMITED)

### 높음
4. **AI 매칭** (성능, 정확도)
5. **파일 업로드** (크기, 시간 제한)
6. **예외 처리** (JWT 만료, 입력 검증)

### 중간
7. **동시성** (메시지 유실 방지)
8. **성능** (대규모 데이터 처리)
9. **E2E** (전체 사용자 플로우)

### 낮음
10. 단순 CRUD
11. Getter/Setter
12. DTO 변환

---

## 단위 테스트 vs 통합 테스트 vs E2E 테스트

| 테스트 유형 | 목적 | 속도 | 범위 | 예시 |
|-----------|-----|------|-----|-----|
| **단위 테스트** | 개별 메서드/클래스 검증 | 빠름 | 좁음 | Service 메서드 1개 |
| **통합 테스트** | 여러 컴포넌트 협업 검증 | 중간 | 중간 | Controller + Service + DB |
| **E2E 테스트** | 전체 사용자 시나리오 검증 | 느림 | 넓음 | 회원가입 → 프로필 → 매칭 |

---

## 외부 서비스 Mock 전략

### AWS S3 Mock
```java
@MockBean
private S3UploadUtil s3UploadUtil;

when(s3UploadUtil.upload(any(), any()))
        .thenReturn("https://mock-s3.amazonaws.com/test.jpg");
```

### Firebase FCM Mock
```java
@MockBean
private NotificationService notificationService;

doNothing().when(notificationService).sendPushNotification(any(), any());
```

### OAuth Provider Mock (MockWebServer)
```java
mockWebServer.enqueue(new MockResponse()
        .setBody("{\"id\":\"12345\",\"email\":\"test@kakao.com\"}")
        .addHeader("Content-Type", "application/json"));
```

---

## 추가 참고사항

- **Testcontainers**는 Docker를 사용하므로 Docker 설치 필수
- 통합 테스트는 단위 테스트보다 느리므로, CI/CD에서는 병렬 실행 권장
- 민감한 정보(API Key 등)는 테스트 환경 변수로 관리
- `@Transactional`로 자동 롤백되지만, Testcontainers는 테스트 종료 시 자동 삭제됨
- E2E 테스트는 최소한으로 유지하고, 핵심 사용자 플로우만 검증

---

## 참고 자료

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Testcontainers Documentation](https://www.testcontainers.org/)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [AssertJ Documentation](https://assertj.github.io/doc/)
- [MockMvc Documentation](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/test/web/servlet/MockMvc.html)
