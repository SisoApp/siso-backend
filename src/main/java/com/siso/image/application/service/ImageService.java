package com.siso.image.application.service;

import com.siso.image.domain.model.Image;
import com.siso.image.domain.model.FileProcessResult;
import com.siso.image.domain.repository.ImageRepository;
import com.siso.image.dto.ImageRequestDto;
import com.siso.image.dto.ImageResponseDto;
import com.siso.image.infrastructure.properties.ImageProperties;
import com.siso.image.infrastructure.properties.ImageFileHandler;
import com.siso.user.domain.repository.UserRepository;
import com.siso.common.util.UserValidationUtil;
import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
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
    
    /** 사용자 데이터 접근 레이어 */
    private final UserRepository userRepository;
    
    /** 사용자 검증 유틸리티 */
    private final UserValidationUtil userValidationUtil;
    
    /** 이미지 파일 처리 핸들러 */
    private final ImageFileHandler imageFileHandler;
    
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
        Long userId = request.getUserId();
        
        // 사용자 존재 여부 확인
        userValidationUtil.validateUserExists(userId);
        
        // 사용자별 이미지 개수 제한 확인
        long currentImageCount = imageRepository.countByUserId(userId);
        if (currentImageCount >= imageProperties.getMaxImagesPerUser()) {
            throw new ExpectedException(ErrorCode.IMAGE_MAX_COUNT_EXCEEDED);
        }
        
        // 통합 파일 처리: 검증 → 저장
        FileProcessResult result = imageFileHandler.processImageFile(file);
        
        log.info("이미지 업로드 - 사용자: {}, 파일명: {}", userId, result.getServerImageName());
        
        // 엔티티 생성 및 저장
        Image image = Image.builder()
                .userId(userId)
                .path(result.getFileUrl()) // 임시 URL, 저장 후 imageId 기반으로 업데이트
                .serverImageName(result.getServerImageName())
                .originalName(result.getOriginalName())
                .build();
        
        Image savedImage = imageRepository.save(image);
        
        // imageId 기반 URL로 업데이트
        String imageIdBasedUrl = imageProperties.getBaseUrl() + "/api/images/view/" + savedImage.getId();
        savedImage.setPath(imageIdBasedUrl);
        savedImage = imageRepository.save(savedImage);
        
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
        Long userId = request.getUserId();
        
        // 사용자 존재 여부 확인
        userValidationUtil.validateUserExists(userId);
        
        // 기존 이미지 조회
        Image existingImage = imageRepository.findById(id)
                .orElseThrow(() -> new ExpectedException(ErrorCode.IMAGE_NOT_FOUND));
        
        // 이미지 소유자 확인
        userValidationUtil.validateUserOwnership(existingImage.getUserId(), userId);
        
        // 새 파일이 제공된 경우에만 파일 교체
        String newPath = existingImage.getPath();
        String newServerImageName = existingImage.getServerImageName();
        String newOriginalName = existingImage.getOriginalName();
        
        if (file != null && !file.isEmpty()) {
            // 기존 파일 삭제
            imageFileHandler.deleteImageFile(existingImage.getPath());
            
            // 통합 파일 처리: 검증 → 저장
            FileProcessResult result = imageFileHandler.processImageFile(file);
            
            // imageId 기반 URL 생성 (기존 이미지 ID 사용)
            newPath = imageProperties.getBaseUrl() + "/api/images/view/" + existingImage.getId();
            newServerImageName = result.getServerImageName();
            newOriginalName = result.getOriginalName();
            
            log.info("이미지 파일 교체 완료 - 기존: {}, 새파일: {}", existingImage.getServerImageName(), newServerImageName);
        }
        
        
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
        imageFileHandler.deleteImageFile(image.getPath());
        
        // 데이터베이스에서 레코드 삭제
        imageRepository.delete(image);
        log.info("이미지 삭제 완료 - ID: {}", id);
    }
}
