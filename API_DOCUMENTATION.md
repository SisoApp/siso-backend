# SISO API 명세서

## 📌 목차
- [1. 사용자 관리 (User)](#1-사용자-관리-user)
- [2. 프로필 관리 (Profile)](#2-프로필-관리-profile)
- [3. 관심사 관리 (Interest)](#3-관심사-관리-interest)
- [4. AI 매칭 (Matching)](#4-ai-매칭-matching)
- [5. 통화 관리 (Call)](#5-통화-관리-call)
- [6. 통화 품질 (Call Quality)](#6-통화-품질-call-quality)
- [7. 통화 리뷰 (Call Review)](#7-통화-리뷰-call-review)
- [8. 채팅 (Chat)](#8-채팅-chat)
- [9. 이미지 관리 (Image)](#9-이미지-관리-image)
- [10. 음성 샘플 (Voice Sample)](#10-음성-샘플-voice-sample)
- [11. 알림 (Notification)](#11-알림-notification)

---

## 1. 사용자 관리 (User)

### Base URL
```
/api/users
```

### 1.1 내 정보 조회
**GET** `/api/users/info`

**Description:** 로그인한 사용자의 정보를 조회합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "status": 200,
  "data": {
    "id": 1,
    "email": "user@example.com",
    "provider": "KAKAO",
    "phoneNumber": "010-1234-5678",
    "presenceStatus": "ONLINE",
    "registrationStatus": "LOGIN"
  },
  "errorMessage": null
}
```

---

### 1.2 알림 설정 변경
**PATCH** `/api/users/notification`

**Description:** 푸시 알림 수신 동의 여부를 변경합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "subscribed": false
}
```

**Response (200 OK):**
```json
{
  "status": 200,
  "data": null,
  "errorMessage": null
}
```

---

### 1.3 회원 탈퇴
**DELETE** `/api/users/delete`

**Description:** 회원 탈퇴를 진행합니다. (소프트 삭제)

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "status": 200,
  "data": null,
  "errorMessage": null
}
```

---

### 1.4 로그아웃
**POST** `/api/users/logout`

**Description:** 로그아웃을 진행합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "status": 200,
  "data": null,
  "errorMessage": null
}
```

---

## 2. 프로필 관리 (Profile)

### Base URL
```
/api/profiles
```

### 2.1 내 프로필 조회
**GET** `/api/profiles/me`

**Description:** 로그인한 사용자의 프로필 정보를 조회합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "nickname": "홍길동",
  "age": 25,
  "sex": "MALE",
  "mbti": "ENFP",
  "location": "서울시 강남구",
  "introduce": "안녕하세요",
  "drinkingCapacity": "OCCASIONALLY",
  "religion": "NONE",
  "smoke": false,
  "preferenceSex": "FEMALE",
  "meetings": ["CLUB_ACTIVITY", "HOBBY_GROUP"]
}
```

---

### 2.2 프로필 생성
**POST** `/api/profiles`

**Description:** 사용자 프로필을 생성합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "nickname": "홍길동",
  "age": 25,
  "sex": "MALE",
  "mbti": "ENFP",
  "location": "서울시 강남구",
  "introduce": "안녕하세요",
  "drinkingCapacity": "OCCASIONALLY",
  "religion": "NONE",
  "smoke": false,
  "preferenceSex": "FEMALE",
  "meetings": ["CLUB_ACTIVITY", "HOBBY_GROUP"]
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "nickname": "홍길동",
  "age": 25,
  ...
}
```

---

### 2.3 프로필 수정
**PATCH** `/api/profiles`

**Description:** 사용자 프로필을 수정합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "nickname": "홍길동수정",
  "age": 26,
  "introduce": "수정된 소개",
  ...
}
```

**Response (200 OK):**
```json
{
  "id": 1,
  "nickname": "홍길동수정",
  ...
}
```

---

### 2.4 상대방 프로필 조회
**GET** `/api/profiles/user/{targetUserId}`

**Description:** 특정 사용자의 프로필을 조회합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Path Parameters:**
- `targetUserId` (Long): 조회할 사용자 ID

**Response (200 OK):**
```json
{
  "id": 2,
  "nickname": "김철수",
  "age": 27,
  ...
}
```

---

### 2.5 프로필 이미지 조회
**GET** `/api/profiles/images`

**Description:** 사용자의 모든 이미지를 조회합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "url": "https://s3.amazonaws.com/...",
    "presignedUrl": "https://...",
    "createdAt": "2025-01-25T12:00:00"
  }
]
```

---

## 3. 관심사 관리 (Interest)

### Base URL
```
/api/interests
```

### 3.1 내 관심사 목록 조회
**GET** `/api/interests/list`

**Description:** 로그인한 사용자의 관심사 목록을 조회합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "status": 200,
  "data": [
    {
      "interest": "MUSIC"
    },
    {
      "interest": "MOVIES"
    }
  ]
}
```

---

### 3.2 관심사 선택
**POST** `/api/interests/select`

**Description:** 사용자의 관심사를 선택합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Request Body:**
```json
[
  { "interest": "MUSIC" },
  { "interest": "MOVIES" },
  { "interest": "TRAVEL" }
]
```

**Response (200 OK):**
```json
{
  "status": 200,
  "data": null,
  "errorMessage": null
}
```

---

### 3.3 관심사 수정
**PATCH** `/api/interests/update`

**Description:** 사용자의 관심사를 수정합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Request Body:**
```json
[
  { "interest": "READING" },
  { "interest": "COOKING" }
]
```

**Response (200 OK):**
```json
{
  "status": 200,
  "data": null,
  "errorMessage": null
}
```

---

## 4. AI 매칭 (Matching)

### Base URL
```
/api/matching
```

### 4.1 AI 매칭 요청
**POST** `/api/matching/request`

**Description:** AI 알고리즘을 사용하여 사용자와 매칭되는 후보들을 찾습니다. 비동기로 처리되며, 결과는 Redis 캐시에 저장됩니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "requestId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "PENDING",
  "message": "매칭을 시작했습니다. 결과는 잠시 후 조회할 수 있습니다."
}
```

---

### 4.2 매칭 결과 조회
**GET** `/api/matching/results`

**Description:** AI 매칭 결과를 Redis 캐시에서 조회합니다. 매칭이 아직 완료되지 않았다면 404를 반환합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "userId": 1,
  "matches": [
    {
      "candidateId": 2,
      "nickname": "김철수",
      "age": 27,
      "mbti": "INFP",
      "interests": ["영화", "음악"],
      "profileImageUrl": "https://...",
      "matchScore": 0.85
    },
    {
      "candidateId": 3,
      "nickname": "이영희",
      "age": 25,
      "mbti": "ENFJ",
      "interests": ["운동", "여행"],
      "profileImageUrl": "https://...",
      "matchScore": 0.78
    }
  ],
  "generatedAt": "2025-01-25T12:00:00",
  "totalCandidates": 15
}
```

**Response (404 Not Found):**
```json
null
```

---

## 5. 통화 관리 (Call)

### Base URL
```
/api/calls
```

### 5.1 통화 요청
**POST** `/api/calls/request`

**Description:** 상대방에게 통화를 요청합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "receiverId": 2
}
```

**Response (200 OK):**
```json
{
  "status": 200,
  "data": {
    "callId": 1,
    "callerId": 1,
    "receiverId": 2,
    "channelName": "channel-550e8400",
    "token": "agora-token-...",
    "status": "REQUESTED"
  }
}
```

---

### 5.2 통화 수락
**POST** `/api/calls/accept`

**Description:** 수신한 통화를 수락합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "callId": 1
}
```

**Response (200 OK):**
```json
{
  "status": 200,
  "data": {
    "callId": 1,
    "status": "ACCEPT",
    "channelName": "channel-550e8400",
    "token": "agora-token-..."
  }
}
```

---

### 5.3 통화 거절
**POST** `/api/calls/deny`

**Description:** 수신한 통화를 거절합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "callId": 1
}
```

**Response (200 OK):**
```json
{
  "status": 200,
  "data": {
    "callId": 1,
    "status": "DENY"
  }
}
```

---

### 5.4 통화 종료
**POST** `/api/calls/end`

**Description:** 진행 중인 통화를 종료합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "callId": 1
}
```

**Query Parameters:**
- `continueRelationship` (boolean): 채팅 이어가기 여부

**Response (200 OK):**
```json
{
  "status": 200,
  "data": {
    "callId": 1,
    "status": "ENDED",
    "duration": 300
  }
}
```

---

### 5.5 통화 취소
**POST** `/api/calls/cancel`

**Description:** 발신한 통화를 취소합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Query Parameters:**
- `callId` (Long): 취소할 통화 ID

**Response (200 OK):**
```json
{
  "status": 200,
  "data": {
    "callId": 1,
    "status": "CANCELED"
  }
}
```

---

### 5.6 발신 통화 목록 조회
**GET** `/api/calls/caller`

**Description:** 사용자가 발신한 통화 목록을 조회합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "status": 200,
  "data": [
    {
      "callId": 1,
      "receiverId": 2,
      "receiverNickname": "김철수",
      "status": "ENDED",
      "duration": 300,
      "startTime": "2025-01-25T12:00:00",
      "endTime": "2025-01-25T12:05:00"
    }
  ]
}
```

---

### 5.7 수신 통화 목록 조회
**GET** `/api/calls/receiver`

**Description:** 사용자가 수신한 통화 목록을 조회합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "status": 200,
  "data": [
    {
      "callId": 2,
      "callerId": 3,
      "callerNickname": "이영희",
      "status": "ENDED",
      "duration": 180,
      "startTime": "2025-01-25T14:00:00",
      "endTime": "2025-01-25T14:03:00"
    }
  ]
}
```

---

## 6. 통화 품질 (Call Quality)

### Base URL
```
/api/call-quality
```

### 6.1 통화 품질 메트릭 제출
**POST** `/api/call-quality/metrics`

**Description:** 클라이언트에서 수집한 WebRTC 통화 품질 데이터를 제출합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "callId": 1,
  "packetLossRate": 2,
  "jitter": 50,
  "roundTripTime": 100,
  "audioBitrate": 64,
  "videoBitrate": 512,
  "audioCodec": "opus",
  "videoCodec": "vp8",
  "clientType": "iOS",
  "networkType": "WiFi"
}
```

**Response (200 OK):**
```json
{
  "status": 200,
  "data": null,
  "errorMessage": null
}
```

---

### 6.2 통화 품질 조회
**GET** `/api/call-quality/metrics/{callId}`

**Description:** 특정 통화의 품질 메트릭 목록을 조회합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Path Parameters:**
- `callId` (Long): 통화 ID

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "callId": 1,
    "packetLossRate": 2,
    "jitter": 50,
    "roundTripTime": 100,
    "audioBitrate": 64,
    "connectionQuality": "EXCELLENT",
    "createdAt": "2025-01-25T12:00:00"
  }
]
```

---

### 6.3 품질 나쁜 통화 조회
**GET** `/api/call-quality/poor-quality`

**Description:** 최근 24시간 내 품질이 나쁜(POOR/BAD) 통화 목록을 조회합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
[
  {
    "id": 5,
    "callId": 10,
    "packetLossRate": 15,
    "jitter": 200,
    "roundTripTime": 500,
    "connectionQuality": "BAD",
    "createdAt": "2025-01-25T10:00:00"
  }
]
```

---

### 6.4 평균 품질 통계
**GET** `/api/call-quality/average`

**Description:** 지정한 기간의 평균 통화 품질 통계를 조회합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Query Parameters:**
- `startDate` (ISO 8601): 시작 날짜 (예: 2025-01-01T00:00:00)
- `endDate` (ISO 8601): 종료 날짜 (예: 2025-01-18T23:59:59)

**Response (200 OK):**
```json
{
  "avgPacketLossRate": 2.5,
  "avgJitter": 55.2,
  "avgRoundTripTime": 105.8,
  "avgAudioBitrate": 62.1
}
```

---

## 7. 통화 리뷰 (Call Review)

### Base URL
```
/api/call-reviews
```

### 7.1 리뷰 작성
**POST** `/api/call-reviews`

**Description:** 통화 후 상대방에 대한 리뷰를 작성합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "callId": 1,
  "targetUserId": 2,
  "rating": 5,
  "comment": "매우 유익한 대화였습니다."
}
```

**Response (200 OK):**
```json
{
  "status": 200,
  "data": {
    "reviewId": 1,
    "rating": 5,
    "comment": "매우 유익한 대화였습니다.",
    "createdAt": "2025-01-25T12:10:00"
  }
}
```

---

### 7.2 리뷰 수정
**PATCH** `/api/call-reviews`

**Description:** 작성한 리뷰를 수정합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "reviewId": 1,
  "rating": 4,
  "comment": "수정된 리뷰 내용"
}
```

**Response (200 OK):**
```json
{
  "status": 200,
  "data": {
    "reviewId": 1,
    "rating": 4,
    "comment": "수정된 리뷰 내용"
  }
}
```

---

### 7.3 내가 받은 리뷰 목록 조회
**GET** `/api/call-reviews/received`

**Description:** 다른 사용자가 나에게 작성한 리뷰 목록을 조회합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "status": 200,
  "data": [
    {
      "reviewId": 2,
      "evaluatorNickname": "김철수",
      "rating": 5,
      "comment": "좋은 대화였습니다.",
      "createdAt": "2025-01-25T14:00:00"
    }
  ]
}
```

---

### 7.4 내가 작성한 리뷰 목록 조회
**GET** `/api/call-reviews/written`

**Description:** 내가 다른 사용자에게 작성한 리뷰 목록을 조회합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "status": 200,
  "data": [
    {
      "reviewId": 1,
      "targetNickname": "이영희",
      "rating": 4,
      "comment": "유익한 시간이었습니다.",
      "createdAt": "2025-01-25T12:10:00"
    }
  ]
}
```

---

### 7.5 상대방이 받은 리뷰 조회
**GET** `/api/call-reviews/other/{userId}`

**Description:** 특정 사용자가 받은 리뷰 목록을 조회합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Path Parameters:**
- `userId` (Long): 조회할 사용자 ID

**Response (200 OK):**
```json
{
  "status": 200,
  "data": [
    {
      "reviewId": 3,
      "rating": 5,
      "comment": "친절하고 좋았습니다.",
      "createdAt": "2025-01-24T10:00:00"
    }
  ]
}
```

---

## 8. 채팅 (Chat)

### Base URL
```
/api/chats
```

### 8.1 채팅방 목록 조회
**GET** `/api/chats/rooms`

**Description:** 사용자의 채팅방 목록을 조회합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "status": 200,
  "data": [
    {
      "chatRoomId": 1,
      "otherUserNickname": "김철수",
      "lastMessage": "안녕하세요",
      "lastMessageTime": "2025-01-25T12:00:00",
      "unreadCount": 3,
      "status": "MATCHED"
    }
  ]
}
```

---

### 8.2 메시지 목록 조회
**GET** `/api/chats/rooms/{chatRoomId}/messages`

**Description:** 특정 채팅방의 메시지 목록을 조회합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Path Parameters:**
- `chatRoomId` (Long): 채팅방 ID

**Query Parameters:**
- `lastMessageId` (Long, optional): 마지막 메시지 ID (페이지네이션)
- `size` (int, default=30): 조회할 메시지 개수

**Response (200 OK):**
```json
{
  "status": 200,
  "data": [
    {
      "messageId": 1,
      "senderId": 2,
      "senderNickname": "김철수",
      "content": "안녕하세요",
      "createdAt": "2025-01-25T12:00:00",
      "deleted": false
    }
  ]
}
```

---

### 8.3 메시지 수정
**PATCH** `/api/chats/messages`

**Description:** 전송한 메시지를 수정합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "messageId": 1,
  "content": "수정된 메시지 내용"
}
```

**Response (200 OK):**
```json
{
  "status": 200,
  "data": {
    "messageId": 1,
    "content": "수정된 메시지 내용",
    "updatedAt": "2025-01-25T12:05:00"
  }
}
```

---

### 8.4 메시지 삭제
**DELETE** `/api/chats/messages/{messageId}`

**Description:** 전송한 메시지를 삭제합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Path Parameters:**
- `messageId` (Long): 삭제할 메시지 ID

**Response (200 OK):**
```json
{
  "status": 200,
  "data": null,
  "errorMessage": null
}
```

---

### 8.5 채팅 이어나가기
**POST** `/api/chats/accept`

**Description:** 제한된 채팅방을 무제한으로 전환합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "chatRoomId": 1
}
```

**Response (200 OK):**
```json
{
  "status": 200,
  "data": null,
  "errorMessage": null
}
```

---

### 8.6 채팅방 나가기
**POST** `/api/chats/leave`

**Description:** 채팅방에서 나갑니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "chatRoomId": 1
}
```

**Response (200 OK):**
```json
{
  "status": 200,
  "data": null,
  "errorMessage": null
}
```

---

### 8.7 채팅방 멤버 조회
**GET** `/api/chats/rooms/{chatRoomId}/members`

**Description:** 채팅방의 멤버 목록을 조회합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Path Parameters:**
- `chatRoomId` (Long): 채팅방 ID

**Response (200 OK):**
```json
{
  "status": 200,
  "data": [
    {
      "userId": 1,
      "nickname": "홍길동",
      "status": "ACTIVE"
    },
    {
      "userId": 2,
      "nickname": "김철수",
      "status": "ACTIVE"
    }
  ]
}
```

---

### 8.8 채팅 제한 정보 조회
**GET** `/api/chats/rooms/limits`

**Description:** 채팅방의 메시지 전송 제한 정보를 조회합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Query Parameters:**
- `chatRoomId` (Long): 채팅방 ID

**Response (200 OK):**
```json
{
  "status": 200,
  "data": {
    "chatRoomId": 1,
    "remainingMessages": 3,
    "totalLimit": 5,
    "unlimited": false
  }
}
```

---

## 9. 이미지 관리 (Image)

### Base URL
```
/api/images
```

### 9.1 이미지 업로드
**POST** `/api/images/upload`

**Description:** 이미지를 업로드합니다. (최대 5개)

**Headers:**
```
Authorization: Bearer {token}
Content-Type: multipart/form-data
```

**Request Body (Form Data):**
- `files`: MultipartFile[] (최대 5개)

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "url": "https://s3.amazonaws.com/.../image1.jpg",
    "presignedUrl": "https://...",
    "createdAt": "2025-01-25T12:00:00"
  }
]
```

---

### 9.2 내 이미지 목록 조회
**GET** `/api/images/me`

**Description:** 로그인한 사용자의 이미지 목록을 조회합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "url": "https://s3.amazonaws.com/.../image1.jpg",
    "presignedUrl": "https://...",
    "createdAt": "2025-01-25T12:00:00"
  }
]
```

---

### 9.3 내 이미지 목록 조회 (경량화)
**GET** `/api/images/me/lightweight`

**Description:** Presigned URL만 포함하여 반환합니다. 매칭 시스템에서 사용하기 적합합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "presignedUrl": "https://...",
    "expiresAt": "2025-01-25T22:00:00"
  }
]
```

---

### 9.4 이미지 단일 조회
**GET** `/api/images/{imageId}`

**Description:** 특정 이미지 정보를 조회합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Path Parameters:**
- `imageId` (Long): 이미지 ID

**Response (200 OK):**
```json
{
  "id": 1,
  "url": "https://s3.amazonaws.com/.../image1.jpg",
  "presignedUrl": "https://...",
  "createdAt": "2025-01-25T12:00:00"
}
```

---

### 9.5 이미지 수정
**PUT** `/api/images/{imageId}`

**Description:** 이미지를 수정(교체)합니다.

**Headers:**
```
Authorization: Bearer {token}
Content-Type: multipart/form-data
```

**Path Parameters:**
- `imageId` (Long): 수정할 이미지 ID

**Request Body (Form Data):**
- `file`: MultipartFile (optional)

**Response (200 OK):**
```json
{
  "id": 1,
  "url": "https://s3.amazonaws.com/.../new-image.jpg",
  "presignedUrl": "https://...",
  "updatedAt": "2025-01-25T13:00:00"
}
```

---

### 9.6 이미지 삭제
**DELETE** `/api/images/{imageId}`

**Description:** 이미지를 삭제합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Path Parameters:**
- `imageId` (Long): 삭제할 이미지 ID

**Response (204 No Content)**

---

### 9.7 Presigned URL 갱신
**POST** `/api/images/refresh-expired-urls`

**Description:** 만료된 Presigned URL들을 일괄 갱신합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
"사용자 1의 만료된 Presigned URL 3개가 갱신되었습니다."
```

---

## 10. 음성 샘플 (Voice Sample)

### Base URL
```
/api/voice-samples
```

### 10.1 음성 업로드
**POST** `/api/voice-samples/upload`

**Description:** 음성 샘플을 업로드합니다. (사용자당 최대 1개, 최대 20초)

**Headers:**
```
Authorization: Bearer {token}
Content-Type: multipart/form-data
```

**Request Body (Form Data):**
- `file`: MultipartFile (음성 파일)

**Response (200 OK):**
```json
{
  "id": 1,
  "userId": 1,
  "url": "https://s3.amazonaws.com/.../voice.mp3",
  "presignedUrl": "https://...",
  "duration": 15,
  "createdAt": "2025-01-25T12:00:00"
}
```

---

### 10.2 내 음성 샘플 조회
**GET** `/api/voice-samples/me`

**Description:** 로그인한 사용자의 음성 샘플 목록을 조회합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "url": "https://s3.amazonaws.com/.../voice.mp3",
    "presignedUrl": "https://...",
    "duration": 15,
    "createdAt": "2025-01-25T12:00:00"
  }
]
```

---

### 10.3 음성 샘플 수정
**PUT** `/api/voice-samples/{voiceId}`

**Description:** 음성 샘플을 수정(교체)합니다.

**Headers:**
```
Authorization: Bearer {token}
Content-Type: multipart/form-data
```

**Path Parameters:**
- `voiceId` (Long): 수정할 음성 샘플 ID

**Request Body (Form Data):**
- `file`: MultipartFile (optional)

**Response (200 OK):**
```json
{
  "id": 1,
  "url": "https://s3.amazonaws.com/.../new-voice.mp3",
  "presignedUrl": "https://...",
  "duration": 18,
  "updatedAt": "2025-01-25T13:00:00"
}
```

---

### 10.4 음성 샘플 삭제
**DELETE** `/api/voice-samples/{voiceId}`

**Description:** 음성 샘플을 삭제합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Path Parameters:**
- `voiceId` (Long): 삭제할 음성 샘플 ID

**Response (204 No Content)**

---

### 10.5 음성 Presigned URL 생성
**GET** `/api/voice-samples/{voiceId}/presigned-url`

**Description:** 10분간 유효한 음성 파일 접근 URL을 생성합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Path Parameters:**
- `voiceId` (Long): 음성 샘플 ID

**Response (200 OK):**
```json
"https://s3.amazonaws.com/...?X-Amz-Expires=600&..."
```

---

### 10.6 음성 단기 재생용 Presigned URL 생성
**GET** `/api/voice-samples/{voiceId}/presigned-url/short-play`

**Description:** 3분간 유효한 음성 파일 접근 URL을 생성합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Path Parameters:**
- `voiceId` (Long): 음성 샘플 ID

**Response (200 OK):**
```json
"https://s3.amazonaws.com/...?X-Amz-Expires=180&..."
```

---

### 10.7 음성 Presigned URL 일괄 갱신
**POST** `/api/voice-samples/refresh-expired-urls`

**Description:** 만료된 Presigned URL들을 일괄 갱신합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
"사용자 1의 만료된 음성 샘플 Presigned URL 1개가 갱신되었습니다."
```

---

## 11. 알림 (Notification)

### Base URL
```
/api/notifications
```

### 11.1 알림 목록 조회
**GET** `/api/notifications`

**Description:** 현재 사용자의 모든 알림을 최신순으로 조회합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "status": 200,
  "data": [
    {
      "notificationId": 1,
      "title": "새로운 매칭",
      "message": "김철수님과 매칭되었습니다.",
      "type": "MATCHING",
      "isRead": false,
      "url": "/matching/results",
      "createdAt": "2025-01-25T12:00:00"
    }
  ]
}
```

---

### 11.2 읽지 않은 알림 조회
**GET** `/api/notifications/unread`

**Description:** 현재 사용자의 읽지 않은 알림을 최신순으로 조회합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "status": 200,
  "data": [
    {
      "notificationId": 1,
      "title": "새로운 매칭",
      "message": "김철수님과 매칭되었습니다.",
      "type": "MATCHING",
      "isRead": false,
      "createdAt": "2025-01-25T12:00:00"
    }
  ]
}
```

---

### 11.3 읽지 않은 알림 개수 조회
**GET** `/api/notifications/unread/count`

**Description:** 현재 사용자의 읽지 않은 알림 개수를 조회합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "status": 200,
  "data": {
    "unreadCount": 5
  }
}
```

---

### 11.4 알림 읽음 처리
**PATCH** `/api/notifications/{notificationId}/read`

**Description:** 특정 알림을 읽음 상태로 변경합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Path Parameters:**
- `notificationId` (Long): 알림 ID

**Response (200 OK):**
```json
{
  "status": 200,
  "data": null,
  "errorMessage": null
}
```

---

### 11.5 모든 알림 읽음 처리
**PATCH** `/api/notifications/read-all`

**Description:** 현재 사용자의 모든 알림을 읽음 상태로 변경합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "status": 200,
  "data": null,
  "errorMessage": null
}
```

---

### 11.6 알림 생성
**POST** `/api/notifications`

**Description:** 새로운 알림을 생성하고 FCM을 통해 전송합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "receiverId": 2,
  "title": "새로운 메시지",
  "message": "홍길동님이 메시지를 보냈습니다.",
  "type": "CHAT",
  "url": "/chat/rooms/1"
}
```

**Response (200 OK):**
```json
{
  "status": 200,
  "data": {
    "notificationId": 1,
    "title": "새로운 메시지",
    "message": "홍길동님이 메시지를 보냈습니다.",
    "type": "CHAT",
    "createdAt": "2025-01-25T12:00:00"
  }
}
```

---

### 11.7 매칭 알림 전송
**POST** `/api/notifications/matching`

**Description:** 새로운 매칭 알림을 생성하고 전송합니다.

**Headers:**
```
Authorization: Bearer {token}
```

**Query Parameters:**
- `receiverId` (Long): 수신자 ID

**Response (200 OK):**
```json
{
  "status": 200,
  "data": {
    "notificationId": 2,
    "title": "새로운 매칭",
    "message": "홍길동님과 매칭되었습니다.",
    "type": "MATCHING",
    "createdAt": "2025-01-25T12:00:00"
  }
}
```

---

## 📝 공통 응답 형식

### 성공 응답
```json
{
  "status": 200,
  "data": { ... },
  "errorMessage": null
}
```

### 에러 응답
```json
{
  "status": 404,
  "data": null,
  "errorMessage": "사용자를 찾을 수 없습니다."
}
```

---

## 🔐 인증

대부분의 API는 JWT 토큰을 통한 인증이 필요합니다.

**Header:**
```
Authorization: Bearer {JWT_TOKEN}
```

**에러 코드:**
- `401 Unauthorized`: 인증 실패
- `403 Forbidden`: 권한 없음
- `404 Not Found`: 리소스 없음
- `400 Bad Request`: 잘못된 요청

---

## 📌 주요 비즈니스 규칙

### 이미지
- 사용자당 최대 5개
- 최대 파일 크기: 10MB
- 지원 형식: JPG, PNG, GIF

### 음성 샘플
- 사용자당 최대 1개
- 최대 길이: 20초
- 지원 형식: MP3, WAV, M4A, OGG

### 채팅
- 매칭 전: 5회 제한
- 매칭 후: 무제한

### AI 매칭
- 비동기 처리 (RabbitMQ)
- 결과 캐싱 (Redis, 30분)
- 상위 20명 추천

---

**마지막 업데이트:** 2025-01-25
**버전:** 1.0.0
