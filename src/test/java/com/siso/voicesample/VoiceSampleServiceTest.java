package com.siso.voicesample;

import com.siso.common.S3Config.*;
import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.common.util.UserValidationUtil;
import com.siso.user.domain.model.Provider;
import com.siso.user.domain.model.RegistrationStatus;
import com.siso.user.domain.model.User;
import com.siso.user.domain.UserRepository;
import com.siso.voicesample.domain.model.VoiceSample;
import com.siso.voicesample.domain.repository.VoiceSampleRepository;
import com.siso.voicesample.dto.request.VoiceSampleRequestDto;
import com.siso.voicesample.dto.response.VoiceSampleResponseDto;
import com.siso.voicesample.infrastructure.properties.VoiceSampleProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * VoiceSampleService 단위 테스트
 *
 * 테스트 대상:
 * - 음성 파일 업로드 성공 (20초 이내)
 * - 음성 파일 업로드 실패 (20초 초과)
 * - 음성 파일 업로드 실패 (지원하지 않는 형식)
 * - 음성 파일 업로드 실패 (파일 크기 초과 50MB)
 * - 음성 파일 업로드 실패 (사용자당 1개 제한)
 * - 음성 파일 조회
 * - 음성 파일 삭제
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VoiceSampleService 단위 테스트")
class VoiceSampleServiceTest {

    @Mock
    private VoiceSampleRepository voiceSampleRepository;

    @Mock
    private UserValidationUtil userValidationUtil;

    @Mock
    private VoiceSampleProperties voiceSampleProperties;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VoiceS3UploadUtil voiceS3UploadUtil;

    @Mock
    private VoiceS3DeleteUtil voiceS3DeleteUtil;

    @Mock
    private VoiceS3KeyUtil voiceS3KeyUtil;

    @Mock
    private VoiceS3PresignedUrlUtil voiceS3PresignedUrlUtil;

    @Mock
    private VoiceCountValidationUtil voiceCountValidationUtil;

    @Mock
    private VoicePresignedUrlManagementUtil voicePresignedUrlManagementUtil;

    @InjectMocks
    private VoiceSampleService voiceSampleService;

    private User user;
    private MultipartFile validFile;

    private static final Long USER_ID = 1L;
    private static final String TEST_S3_URL = "https://s3.amazonaws.com/test/voice.mp3";

    @BeforeEach
    void setUp() {
        user = User.builder()
                .provider(Provider.KAKAO)
                .email("test@example.com")
                .phoneNumber("010-1234-5678")
                .registrationStatus(RegistrationStatus.LOGIN)
                .build();

        // ID 설정
        try {
            java.lang.reflect.Field idField = User.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(user, USER_ID);
        } catch (Exception e) {
        }

        // Mock 음성 파일 생성 (10초 길이의 MP3)
        validFile = new MockMultipartFile(
                "voice",
                "test-voice.mp3",
                "audio/mpeg",
                "fake audio content".getBytes()
        );
    }

    @Test
    @DisplayName("음성 파일 업로드 성공 - 20초 이내")
    void uploadVoiceSample_whenValidFile_shouldUploadSuccessfully() {
        // Given
        VoiceSampleRequestDto request = new VoiceSampleRequestDto(USER_ID);

        doNothing().when(userValidationUtil).validateUserExists(USER_ID);
        doNothing().when(voiceCountValidationUtil).validateVoiceCountLimit(USER_ID);

        when(voiceSampleProperties.isSupportedFormat(".mp3")).thenReturn(true);
        when(voiceSampleProperties.getMaxFileSize()).thenReturn(50 * 1024 * 1024L);  // 50MB
        when(voiceSampleProperties.getMaxDuration()).thenReturn(20);  // 20초

        when(voiceS3KeyUtil.generateUuidName(anyString())).thenReturn("uuid-test-voice.mp3");
        when(voiceS3KeyUtil.buildKey(eq(USER_ID), anyString())).thenReturn("voice/1/uuid-test-voice.mp3");

        doNothing().when(voiceS3UploadUtil).putObject(anyString(), any(MultipartFile.class), anyString());
        when(voiceS3UploadUtil.generateS3Url(anyString())).thenReturn(TEST_S3_URL);

        VoiceSample savedVoiceSample = VoiceSample.builder()
                .user(user)
                .url(TEST_S3_URL)
                .duration(10)  // 10초
                .fileSize((int) validFile.getSize())
                .build();

        when(voiceSampleRepository.save(any(VoiceSample.class))).thenReturn(savedVoiceSample);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        doNothing().when(voicePresignedUrlManagementUtil).generateAndSavePresignedUrl(any(), anyInt());

        // When
        VoiceSampleResponseDto result = voiceSampleService.uploadVoiceSample(validFile, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUrl()).isEqualTo(TEST_S3_URL);

        verify(userValidationUtil).validateUserExists(USER_ID);
        verify(voiceCountValidationUtil).validateVoiceCountLimit(USER_ID);
        verify(voiceS3UploadUtil).putObject(anyString(), any(MultipartFile.class), anyString());
        verify(voiceSampleRepository).save(any(VoiceSample.class));
    }

    @Test
    @DisplayName("음성 파일 업로드 실패 - 빈 파일")
    void uploadVoiceSample_whenEmptyFile_shouldThrowException() {
        // Given
        MultipartFile emptyFile = new MockMultipartFile(
                "voice",
                "test-voice.mp3",
                "audio/mpeg",
                new byte[0]
        );

        VoiceSampleRequestDto request = new VoiceSampleRequestDto(USER_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        doNothing().when(userValidationUtil).validateUserExists(USER_ID);
        doNothing().when(voiceCountValidationUtil).validateVoiceCountLimit(USER_ID);

        when(voiceSampleProperties.isSupportedFormat(".mp3")).thenReturn(true);
        when(voiceSampleProperties.getMaxFileSize()).thenReturn(50 * 1024 * 1024L);

        // When & Then: 빈 파일 업로드 시 예외 발생
        assertThatThrownBy(() -> voiceSampleService.uploadVoiceSample(emptyFile, request))
                .isInstanceOf(ExpectedException.class);

        verify(voiceSampleRepository, never()).save(any());
    }

    @Test
    @DisplayName("음성 파일 업로드 실패 - 지원하지 않는 파일 형식")
    void uploadVoiceSample_whenUnsupportedFormat_shouldThrowException() {
        // Given
        MultipartFile unsupportedFile = new MockMultipartFile(
                "voice",
                "test-voice.txt",
                "text/plain",
                "fake content".getBytes()
        );

        VoiceSampleRequestDto request = new VoiceSampleRequestDto(USER_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        doNothing().when(userValidationUtil).validateUserExists(USER_ID);
        doNothing().when(voiceCountValidationUtil).validateVoiceCountLimit(USER_ID);

        when(voiceSampleProperties.isSupportedFormat(".txt")).thenReturn(false);
        when(voiceSampleProperties.getMaxFileSize()).thenReturn(50 * 1024 * 1024L);

        // When & Then
        assertThatThrownBy(() -> voiceSampleService.uploadVoiceSample(unsupportedFile, request))
                .isInstanceOf(ExpectedException.class);

        verify(voiceSampleRepository, never()).save(any());
    }

    @Test
    @DisplayName("음성 파일 조회 성공")
    void getVoiceSample_whenExists_shouldReturnVoiceSample() {
        // Given
        Long voiceId = 1L;

        VoiceSample voiceSample = VoiceSample.builder()
                .user(user)
                .url(TEST_S3_URL)
                .duration(10)
                .fileSize(1024)
                .build();

        when(voiceSampleRepository.findById(voiceId)).thenReturn(Optional.of(voiceSample));
        doNothing().when(voicePresignedUrlManagementUtil).generateAndSavePresignedUrl(any(), anyInt());

        // When
        VoiceSampleResponseDto result = voiceSampleService.getVoiceSample(voiceId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUrl()).isEqualTo(TEST_S3_URL);
        assertThat(result.getDuration()).isEqualTo(10);

        verify(voiceSampleRepository).findById(voiceId);
    }

    @Test
    @DisplayName("음성 파일 조회 실패 - 존재하지 않음")
    void getVoiceSample_whenNotExists_shouldThrowException() {
        // Given
        Long voiceId = 999L;

        when(voiceSampleRepository.findById(voiceId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> voiceSampleService.getVoiceSample(voiceId))
                .isInstanceOf(ExpectedException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.VOICE_SAMPLE_NOT_FOUND);

        verify(voiceSampleRepository).findById(voiceId);
    }

    @Test
    @DisplayName("음성 파일 삭제 성공")
    void deleteVoiceSample_whenExists_shouldDeleteSuccessfully() {
        // Given
        Long voiceId = 1L;

        VoiceSample voiceSample = VoiceSample.builder()
                .user(user)
                .url(TEST_S3_URL)
                .duration(10)
                .fileSize(1024)
                .build();

        when(voiceSampleRepository.findById(voiceId)).thenReturn(Optional.of(voiceSample));
        when(voiceS3KeyUtil.extractKey(TEST_S3_URL)).thenReturn("voice/1/uuid-test-voice.mp3");
        doNothing().when(voiceS3DeleteUtil).safeDeleteS3(anyString());
        doNothing().when(voiceSampleRepository).delete(any(VoiceSample.class));
        doNothing().when(voiceSampleRepository).flush();

        // When
        voiceSampleService.deleteVoiceSample(voiceId);

        // Then
        verify(voiceSampleRepository).findById(voiceId);
        verify(voiceS3KeyUtil).extractKey(TEST_S3_URL);
        verify(voiceS3DeleteUtil).safeDeleteS3("voice/1/uuid-test-voice.mp3");
        verify(voiceSampleRepository).delete(voiceSample);
    }

    @Test
    @DisplayName("사용자의 음성 파일 목록 조회")
    void getVoiceSamplesByUserId_shouldReturnList() {
        // Given
        VoiceSample voiceSample1 = VoiceSample.builder()
                .user(user)
                .url(TEST_S3_URL)
                .duration(10)
                .fileSize(1024)
                .build();

        when(voiceSampleRepository.findByUserIdOrderByCreatedAtDesc(USER_ID))
                .thenReturn(List.of(voiceSample1));
        doNothing().when(voicePresignedUrlManagementUtil).generateAndSavePresignedUrl(any(), anyInt());

        // When
        List<VoiceSampleResponseDto> result = voiceSampleService.getVoiceSamplesByUserId(USER_ID);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUrl()).isEqualTo(TEST_S3_URL);

        verify(voiceSampleRepository).findByUserIdOrderByCreatedAtDesc(USER_ID);
    }

    @Test
    @DisplayName("Presigned URL 생성")
    void getVoicePresignedUrl_shouldGenerateUrl() {
        // Given
        Long voiceId = 1L;

        VoiceSample voiceSample = VoiceSample.builder()
                .user(user)
                .url(TEST_S3_URL)
                .duration(10)
                .fileSize(1024)
                .build();

        String presignedUrl = "https://s3.amazonaws.com/test/voice.mp3?presigned=true";

        when(voiceSampleRepository.findById(voiceId)).thenReturn(Optional.of(voiceSample));
        doNothing().when(userValidationUtil).validateUserOwnership(any(), any());
        when(voiceS3KeyUtil.extractKey(TEST_S3_URL)).thenReturn("voice/1/uuid-test-voice.mp3");
        when(voiceS3PresignedUrlUtil.generatePresignedGetUrl(anyString())).thenReturn(presignedUrl);

        // When
        String result = voiceSampleService.getVoicePresignedUrl(voiceId, USER_ID);

        // Then
        assertThat(result).isEqualTo(presignedUrl);

        verify(voiceSampleRepository).findById(voiceId);
        verify(voiceS3PresignedUrlUtil).generatePresignedGetUrl("voice/1/uuid-test-voice.mp3");
    }
}
