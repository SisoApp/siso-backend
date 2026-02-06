# SISO Backend

소개팅 / 음성 매칭 서비스 SISO의 백엔드 서버입니다.

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 17 |
| Framework | Spring Boot 3.5.4 |
| Database | MariaDB / MySQL 8.0 |
| ORM | Spring Data JPA, QueryDSL |
| Cache | Redis, Caffeine |
| Message Queue | RabbitMQ |
| Auth | JWT (jjwt), OAuth2 (Kakao) |
| Real-time | WebSocket (STOMP) |
| Call | Agora RTC SDK |
| Storage | AWS S3 |
| Push | Firebase Cloud Messaging |
| CI/CD | GitHub Actions, AWS CodeDeploy |
| Infra | Docker, Docker Compose |
| Test | JUnit 5, Testcontainers, AssertJ, Mockito |
| Docs | Swagger (springdoc-openapi) |

## 프로젝트 구조

```
src/main/java/com/siso/
├── call/              # 음성 통화 (Agora RTC)
├── callreview/        # 통화 리뷰
├── chat/              # 채팅 (WebSocket + RabbitMQ)
├── common/            # 공통 설정, 보안, 예외 처리
├── image/             # 이미지 관리 (S3)
├── matching/          # AI 매칭 알고리즘
├── notification/      # 푸시 알림 (FCM)
├── report/            # 신고
├── user/              # 사용자, 인증, 프로필
└── voicesample/       # 음성 샘플 (S3)
```

## 핵심 기능

### AI 매칭 알고리즘

6가지 지표를 가중치 기반으로 계산하여 상위 20명을 추천합니다.

| 지표 | 가중치 | 알고리즘 |
|------|--------|----------|
| 관심사 유사도 | 30% | Jaccard Similarity |
| 나이 호환성 | 20% | 거리 기반 감쇠 |
| MBTI 호환성 | 15% | 궁합 매트릭스 |
| 지역 근접성 | 15% | 지역 단위 비교 |
| 활동성 | 10% | 최근 접속 시간 기반 |
| 생활습관 | 10% | 음주/흡연 호환성 |

- RabbitMQ를 통한 비동기 처리 (Consumer 3~10개 병렬)
- Redis 캐싱 (TTL 10분)으로 재조회 최적화

### 채팅 시스템

- WebSocket(STOMP) 실시간 메시지 전송
- RabbitMQ로 메시지 안정성 보장 (손실 방지, 자동 재시도)
- 온라인 사용자: WebSocket 전송 / 오프라인 사용자: FCM 푸시 알림
- 매칭 전 메시지 5회 제한, 매칭 후 무제한

### 음성 통화

- Agora RTC SDK 기반 1:1 음성 통화
- 통화 요청 / 수락 / 거절 / 취소 / 종료 상태 관리
- 통화 종료 후 채팅방 생성 옵션
- 통화 품질 메트릭 수집 (패킷 손실률, 지터, RTT 등)

### 인증

- Kakao OAuth2 소셜 로그인
- JWT Access Token (2시간) + Refresh Token (2주)
- Spring Security 필터 체인 기반 인증

## 로컬 실행

### 1. 인프라 실행 (Docker)

```bash
docker run -d --name siso-mysql -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=test -e MYSQL_DATABASE=siso_test \
  mysql:8.0

docker run -d --name siso-redis -p 6379:6379 \
  redis:7-alpine

docker run -d --name siso-rabbitmq -p 5672:5672 -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=admin -e RABBITMQ_DEFAULT_PASS=admin123 \
  rabbitmq:3.12-management
```

### 2. 애플리케이션 실행

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 3. Swagger UI 접속

```
http://localhost:8080/swagger-ui/index.html
```

## 테스트

### 테스트 구조

```
src/test_unit/          # 단위 테스트 (5개)
src/test_integration/   # 통합 테스트 (12개 테스트 클래스)
```

단위 테스트와 통합 테스트를 Gradle 커스텀 소스셋으로 분리하여 관리합니다.
통합 테스트는 Testcontainers로 실제 MySQL 컨테이너를 사용합니다.

### 실행

```bash
# 단위 테스트
./gradlew unitTest

# 통합 테스트
./gradlew integrationTest

# 전체 테스트
./gradlew check
```

### 테스트 목록

**단위 테스트**

| 파일 | 대상 |
|------|------|
| `JwtTokenUtilTest` | JWT 토큰 생성/검증/만료 |
| `OAuthServiceTest` | OAuth2 인증 로직 |
| `MatchingAlgorithmServiceTest` | 매칭 스코어 6가지 계산 |
| `AgoraCallServiceTest` | 통화 상태 전환 로직 |
| `ChatMessageServiceTest` | 메시지 전송/제한 로직 |

**통합 테스트 - API**

| 파일 | 대상 |
|------|------|
| `UserControllerIntegrationTest` | 사용자 API (조회, 로그아웃, 탈퇴) |
| `MatchingControllerIntegrationTest` | 매칭 API |
| `CallQualityControllerIntegrationTest` | 통화 품질 API |
| `InputValidationTest` | 입력값 검증 (@Valid) |
| `JwtAuthenticationExceptionTest` | JWT 인증 예외 |

**통합 테스트 - Infrastructure**

| 파일 | 대상 |
|------|------|
| `AgoraCallServiceIntegrationTest` | 통화 전체 플로우 (요청-수락-종료) |
| `MatchingAlgorithmIntegrationTest` | 매칭 알고리즘 DB 연동 |
| `MatchingAlgorithmPerformanceTest` | 매칭 성능 (1000명 기준) |
| `ChatMessageQueueIntegrationTest` | RabbitMQ 메시지 큐 |
| `ConcurrencyTest` | 동시성 (100개 동시 메시지) |

**통합 테스트 - Repository**

| 파일 | 대상 |
|------|------|
| `UserRepositoryIntegrationTest` | 사용자 조회 쿼리 |

## CI/CD

GitHub Actions를 통해 push/PR 시 자동으로 테스트를 실행합니다.

```
push/PR (main, develop)
  └─ test job
       ├─ unitTest (단위 테스트)
       └─ integrationTest (통합 테스트)
            └─ 전부 통과 시 → build-and-deploy (배포)
```

- 통합 테스트는 Testcontainers(MySQL) + CI 서비스(Redis, RabbitMQ)로 실행
- 배포: JAR 빌드 → S3 업로드 → AWS CodeDeploy → EC2 (main 브랜치만)
- 배포 파이프라인은 AWS 인프라 구성 후 주석 해제하여 사용

## API 문서

상세 API 명세는 [API_DOCUMENTATION.md](./API_DOCUMENTATION.md)를 참고하세요.

메시지 큐 아키텍처는 [MESSAGE_QUEUE_DESIGN.md](./MESSAGE_QUEUE_DESIGN.md)를 참고하세요.
