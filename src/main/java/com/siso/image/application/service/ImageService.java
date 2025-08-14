package com.siso.image.application.service;

import com.siso.image.domain.model.Image;
import com.siso.image.domain.repository.ImageRepository;
import com.siso.image.dto.ImageRequestDto;
import com.siso.image.dto.ImageResponseDto;
import com.siso.image.infrastructure.properties.ImageProperties;
import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 이미지 비즈니스 로직 처리 서비스
 * 
 * 주요 기능:
 * - 이미지 파일 업로드 및 저장 (사용자당 최대 5개)
 * - 이미지 CRUD 작업 (생성, 조회, 수정, 삭제)
 * - 파일 저장소 관리 (로컬 파일 시스템)
 * 
 * 지원 파일 형식: JPG, JPEG, PNG, GIF, WEBP
 * 파일 크기 제한: 10MB
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageService {
    
    // === 의존성 주입 ===
    /** 이미지 데이터 접근 레이어 */
    private final ImageRepository imageRepository;
    
    /** 이미지 관련 설정 프로퍼티 */
    private final ImageProperties imageProperties;
    
    // ===================== 공개 API 메서드들 =====================
    
    /**
     * 이미지 파일 업로드 및 저장
     * 
     * 처리 과정:
     * 1. 사용자별 이미지 개수 제한 확인 (최대 5개)
     * 2. 파일 검증 (형식, 크기)
     * 3. 고유 파일명으로 저장
     * 4. 데이터베이스에 메타데이터 저장
     * 
     * @param file 업로드할 이미지 파일 (MultipartFile)
     * @param request 사용자 ID 등 추가 정보
     * @return 저장된 이미지 정보
     * @throws IllegalArgumentException 파일 검증 실패 또는 개수 제한 초과 시
     * @throws RuntimeException 파일 저장 실패 시
     */
    @Transactional
    public ImageResponseDto uploadImage(MultipartFile file, ImageRequestDto request) {
        Long userId = request.getUserId() != null ? request.getUserId() : 1L; // 테스트용 기본값
        
        // 사용자별 이미지 개수 제한 확인
        long currentImageCount = imageRepository.countByUserId(userId);
        if (currentImageCount >= imageProperties.getMaxImagesPerUser()) {
            throw new ExpectedException(ErrorCode.IMAGE_MAX_COUNT_EXCEEDED);
        }
        
        // 통합 파일 처리: 검증 → 저장
        FileProcessResult result = processImageFile(file);
        
        log.info("이미지 업로드 - 사용자: {}, 파일명: {}", userId, result.serverImageName);
        
        // 엔티티 생성 및 저장
        Image image = Image.builder()
                .userId(userId)
                .path(result.fileUrl)
                .serverImageName(result.serverImageName)
                .originalName(result.originalName)
                .build();
        
        Image savedImage = imageRepository.save(image);
        
        log.info("이미지 업로드 완료 - ID: {}, 사용자: {}", savedImage.getId(), userId);
        
        return ImageResponseDto.fromEntity(savedImage);
    }
    
    /**
     * 특정 사용자의 이미지 목록 조회
     * 
     * @param userId 조회할 사용자 ID
     * @return 해당 사용자의 이미지 목록 (생성일 기준 오름차순 정렬)
     */
    public List<ImageResponseDto> getImagesByUserId(Long userId) {
        List<Image> images = imageRepository.findByUserIdOrderByCreatedAtAsc(userId);
        return images.stream()
                .map(ImageResponseDto::fromEntity) // 엔티티 → DTO 변환
                .collect(Collectors.toList());
    }
    
    /**
     * 이미지 단일 조회
     * 
     * @param id 조회할 이미지 ID
     * @return 이미지 상세 정보
     * @throws RuntimeException 해당 ID의 이미지가 존재하지 않는 경우
     */
    public ImageResponseDto getImage(Long id) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new ExpectedException(ErrorCode.IMAGE_NOT_FOUND));
        return ImageResponseDto.fromEntity(image);
    }
    
    /**
     * 이미지 수정 (파일 교체)
     * 
     * 처리 과정:
     * 1. 기존 이미지 조회
     * 2. 새 파일이 있는 경우: 기존 파일 삭제 → 새 파일 처리 (검증, 저장)
     * 3. 메타데이터 업데이트 (path, serverImageName, originalName)
     * 4. 데이터베이스 저장 (updatedAt 자동 갱신)
     * 
     * @param id 수정할 이미지 ID
     * @param file 새로운 이미지 파일 (null 가능 - 메타데이터만 수정 시)
     * @param request 수정할 정보 (userId 등)
     * @return 수정된 이미지 정보
     * @throws RuntimeException 해당 ID의 이미지가 존재하지 않는 경우
     */
    @Transactional
    public ImageResponseDto updateImage(Long id, MultipartFile file, ImageRequestDto request) {
        // 기존 이미지 조회
        Image existingImage = imageRepository.findById(id)
                .orElseThrow(() -> new ExpectedException(ErrorCode.IMAGE_NOT_FOUND));
        
        // 새 파일이 제공된 경우에만 파일 교체
        String newPath = existingImage.getPath();
        String newServerImageName = existingImage.getServerImageName();
        String newOriginalName = existingImage.getOriginalName();
        
        if (file != null && !file.isEmpty()) {
            // 기존 파일 삭제
            deleteImageFile(existingImage.getPath());
            
            // 통합 파일 처리: 검증 → 저장
            FileProcessResult result = processImageFile(file);
            
            newPath = result.fileUrl;
            newServerImageName = result.serverImageName;
            newOriginalName = result.originalName;
            
            log.info("이미지 파일 교체 완료 - 기존: {}, 새파일: {}", existingImage.getServerImageName(), newServerImageName);
        }
        
        // userId 처리 (새 값이 있으면 사용, 없으면 기존값 유지)
        Long userId = request.getUserId() != null ? request.getUserId() : existingImage.getUserId();
        
        // 기존 엔티티 업데이트 (BaseTime의 updatedAt 자동 갱신)
        existingImage.setUserId(userId);
        existingImage.setPath(newPath);
        existingImage.setServerImageName(newServerImageName);
        existingImage.setOriginalName(newOriginalName);
        
        Image savedImage = imageRepository.save(existingImage);
        
        log.info("이미지 수정 완료 - ID: {}, 사용자: {}", savedImage.getId(), userId);
        
        return ImageResponseDto.fromEntity(savedImage);
    }
    
    /**
     * 이미지 삭제
     * 
     * 처리 과정:
     * 1. 기존 이미지 조회
     * 2. 파일 시스템에서 이미지 파일 삭제 (실패해도 계속 진행)
     * 3. 데이터베이스에서 레코드 삭제
     * 
     * @param id 삭제할 이미지 ID
     * @throws RuntimeException 해당 ID의 이미지가 존재하지 않는 경우
     */
    @Transactional
    public void deleteImage(Long id) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new ExpectedException(ErrorCode.IMAGE_NOT_FOUND));
        
        // 파일 삭제 (실패해도 DB 삭제는 진행)
        deleteImageFile(image.getPath());
        
        // 데이터베이스에서 레코드 삭제
        imageRepository.delete(image);
        log.info("이미지 삭제 완료 - ID: {}", id);
    }
    
    // ===================== 내부 헬퍼 메서드들 =====================
    
    /**
     * 통합 파일 처리 메서드
     * 
     * 이미지 파일의 전체 처리 과정을 하나의 메서드에서 담당:
     * 1. 파일 검증 (빈 파일, 파일명, 확장자, 크기)
     * 2. 고유 파일명 생성 및 파일 저장
     * 3. 결과 반환 (URL, serverImageName, originalName)
     * 
     * @param file 처리할 이미지 파일
     * @return 파일 처리 결과 (URL, serverImageName, originalName)
     * @throws IllegalArgumentException 파일 검증 실패 시
     * @throws RuntimeException 파일 저장 실패 시
     */
    private FileProcessResult processImageFile(MultipartFile file) {
        try {
            // === 1. 파일 검증 ===
            if (file.isEmpty()) {
                throw new ExpectedException(ErrorCode.IMAGE_FILE_EMPTY);
            }
            
            String originalFileName = file.getOriginalFilename();
            if (originalFileName == null) {
                throw new ExpectedException(ErrorCode.IMAGE_INVALID_FILENAME);
            }
            
            // 지원 형식 검증 (프로퍼티 활용)
            String fileName = originalFileName.toLowerCase();
            String extension = originalFileName.contains(".") 
                    ? originalFileName.substring(originalFileName.lastIndexOf(".")) : "";
            
            if (!imageProperties.isSupportedFormat(extension)) {
                throw new ExpectedException(ErrorCode.IMAGE_UNSUPPORTED_FORMAT);
            }
            
            // 파일 크기 검증 (프로퍼티 활용)
            if (file.getSize() > imageProperties.getMaxFileSize()) {
                throw new ExpectedException(ErrorCode.IMAGE_FILE_TOO_LARGE);
            }
            
            // === 2. 파일 저장 ===
            // 업로드 디렉토리 생성
            Path uploadPath = Paths.get(imageProperties.getUploadDir());
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // 고유 파일명 생성 (타임스탬프 + UUID + 확장자)
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String uniqueFileName = timestamp + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
            
            // 파일 저장
            Path filePath = uploadPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // === 3. 결과 반환 ===
            String fileUrl = imageProperties.getBaseUrl() + "/api/images/files/" + uniqueFileName;
            return new FileProcessResult(fileUrl, uniqueFileName, originalFileName);
            
        } catch (IOException e) {
            log.error("파일 처리 중 오류 발생: {}", e.getMessage());
            throw new ExpectedException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }
    
    /**
     * 이미지 파일 삭제
     * 
     * URL에서 파일명을 추출하여 파일 시스템에서 삭제
     * 실패해도 예외를 던지지 않고 경고 로그만 남김
     * 
     * @param fileUrl 삭제할 파일의 URL
     */
    private void deleteImageFile(String fileUrl) {
        try {
            if (fileUrl != null && fileUrl.contains("/files/")) {
                // URL에서 파일명 추출
                String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
                Path filePath = Paths.get(imageProperties.getUploadDir()).resolve(fileName);
                
                // 파일 존재 시 삭제
                boolean deleted = Files.deleteIfExists(filePath);
                if (deleted) {
                    log.info("이미지 파일 삭제 성공: {}", fileName);
                } else {
                    log.warn("이미지 파일이 존재하지 않음: {}", fileName);
                }
            }
        } catch (Exception e) {
            log.warn("이미지 파일 삭제 실패: {}", e.getMessage());
        }
    }
    
    // ===================== 내부 데이터 클래스 =====================
    
    /**
     * 파일 처리 결과를 담는 내부 클래스
     * 
     * processImageFile() 메서드의 반환값으로 사용
     * 파일 처리 후 생성된 URL, 서버 파일명, 원본 파일명을 포함
     */
    private static class FileProcessResult {
        /** 생성된 파일 접근 URL */
        final String fileUrl;
        /** 서버에 저장된 파일명 */
        final String serverImageName;
        /** 원본 파일명 */
        final String originalName;
        
        /**
         * 파일 처리 결과 생성자
         * 
         * @param fileUrl 파일 접근 URL
         * @param serverImageName 서버 파일명
         * @param originalName 원본 파일명
         */
        FileProcessResult(String fileUrl, String serverImageName, String originalName) {
            this.fileUrl = fileUrl;
            this.serverImageName = serverImageName;
            this.originalName = originalName;
        }
    }
}
