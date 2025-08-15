package com.siso.image.infrastructure.properties;

import com.siso.image.domain.model.FileProcessResult;
import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 이미지 파일 처리 핸들러
 * 
 * 이미지 파일의 모든 물리적 처리를 담당하는 핸들러입니다.
 * Properties 패키지에 위치하여 설정과 밀접한 파일 처리 로직을 관리합니다.
 * 
 * 주요 책임:
 * - 이미지 파일 검증 (형식, 크기, 파일명)
 * - 파일 저장 및 고유 파일명 생성
 * - 파일 삭제 및 URL 생성
 * - 업로드 디렉토리 관리
 * 
 * 이 핸들러는 순수한 파일 처리 로직만을 담당하며,
 * 비즈니스 로직과 완전히 분리되어 있습니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ImageFileHandler {
    
    private final ImageProperties imageProperties;
    
    /**
     * 이미지 파일 통합 처리
     * 
     * 처리 과정:
     * 1. 파일 검증 (빈 파일, 파일명, 확장자, 크기)
     * 2. 사용자별 디렉토리 생성
     * 3. 고유 파일명 생성 및 파일 저장
     * 4. 결과 반환 (URL, serverImageName, originalName)
     * 
     * @param file 처리할 이미지 파일
     * @param userId 사용자 ID (폴더 구조에 사용)
     * @return 파일 처리 결과 (URL, serverImageName, originalName)
     * @throws ExpectedException 파일 검증 실패 시
     * @throws ExpectedException 파일 저장 실패 시
     */
    public FileProcessResult processImageFile(MultipartFile file, Long userId) {
        try {
            // === 1. 파일 검증 ===
            validateFile(file);
            
            String originalFileName = file.getOriginalFilename();
            String extension = extractFileExtension(originalFileName);
            
            validateFileFormat(extension);
            validateFileSize(file.getSize());
            
            // === 2. 파일 저장 ===
            ensureUserUploadDirectoryExists(userId);
            
            String uniqueFileName = generateUniqueFileName(extension);
            Path savedFilePath = saveFile(file, uniqueFileName, userId);
            
            // === 3. 결과 반환 ===
            String fileUrl = generateFileUrl(uniqueFileName);
            
            log.info("이미지 파일 처리 완료 - 원본: {}, 저장: {}", originalFileName, uniqueFileName);
            
            return FileProcessResult.of(fileUrl, uniqueFileName, originalFileName);
            
        } catch (IOException e) {
            log.error("파일 처리 중 I/O 오류 발생: {}", e.getMessage(), e);
            throw new ExpectedException(ErrorCode.IMAGE_UPLOAD_FAILED);
        } catch (ExpectedException e) {
            // 이미 처리된 비즈니스 예외는 그대로 전파
            throw e;
        } catch (Exception e) {
            log.error("파일 처리 중 예상치 못한 오류 발생: {}", e.getMessage(), e);
            throw new ExpectedException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }
    
    /**
     * 이미지 파일 삭제
     * 
     * @param fileUrl 삭제할 파일의 URL (예: http://localhost:8080/api/images/view/filename.jpg)
     * @param userId 사용자 ID (폴더 구조에 사용)
     */
    public void deleteImageFile(String fileUrl, Long userId) {
        if (fileUrl == null || fileUrl.trim().isEmpty()) {
            log.warn("삭제할 파일 URL이 없습니다.");
            return;
        }
        
        try {
            // URL에서 파일명 추출
            String fileName = extractFileNameFromUrl(fileUrl);
            if (fileName == null) {
                log.warn("URL에서 파일명을 추출할 수 없습니다: {}", fileUrl);
                return;
            }
            
            // 사용자별 파일 경로 생성 및 삭제
            Path filePath = Paths.get(imageProperties.getUploadDir()).resolve(userId.toString()).resolve(fileName);
            
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("이미지 파일 삭제 완료: {}", fileName);
            } else {
                log.warn("삭제할 파일이 존재하지 않습니다: {}", filePath);
            }
            
        } catch (Exception e) {
            log.warn("이미지 파일 삭제 실패 - URL: {}, 오류: {}", fileUrl, e.getMessage());
        }
    }
    
    // ===================== 내부 헬퍼 메서드들 =====================
    
    /**
     * 기본 파일 검증 (빈 파일, 파일명)
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ExpectedException(ErrorCode.IMAGE_FILE_EMPTY);
        }
        
        if (file.getOriginalFilename() == null) {
            throw new ExpectedException(ErrorCode.IMAGE_INVALID_FILENAME);
        }
    }
    
    /**
     * 파일 확장자 추출
     */
    private String extractFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
    
    /**
     * 파일 형식 검증
     */
    private void validateFileFormat(String extension) {
        if (!imageProperties.isSupportedFormat(extension)) {
            throw new ExpectedException(ErrorCode.IMAGE_UNSUPPORTED_FORMAT);
        }
    }
    
    /**
     * 파일 크기 검증
     */
    private void validateFileSize(long fileSize) {
        if (fileSize > imageProperties.getMaxFileSize()) {
            throw new ExpectedException(ErrorCode.IMAGE_FILE_TOO_LARGE);
        }
    }
    
    /**
     * 사용자별 업로드 디렉토리 존재 확인 및 생성
     */
    private void ensureUserUploadDirectoryExists(Long userId) throws IOException {
        Path userUploadPath = Paths.get(imageProperties.getUploadDir()).resolve(userId.toString());
        if (!Files.exists(userUploadPath)) {
            Files.createDirectories(userUploadPath);
            log.info("사용자별 업로드 디렉토리 생성: {}", userUploadPath);
        }
    }
    
    /**
     * 고유 파일명 생성
     */
    private String generateUniqueFileName(String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return timestamp + "_" + uuid + extension;
    }
    
    /**
     * 파일 저장 (사용자별 폴더)
     */
    private Path saveFile(MultipartFile file, String uniqueFileName, Long userId) throws IOException {
        Path userUploadPath = Paths.get(imageProperties.getUploadDir()).resolve(userId.toString());
        Path filePath = userUploadPath.resolve(uniqueFileName);
        
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        return filePath;
    }
    
    /**
     * 임시 파일 URL 생성 (실제 사용 시에는 imageId 기반으로 변경됨)
     * 
     * 참고: 실제 뷰어에서는 /api/images/view/{imageId} 형태로 사용되며,
     * 이 URL은 파일 저장 시점에서만 임시로 생성됩니다.
     */
    private String generateFileUrl(String fileName) {
        return imageProperties.getBaseUrl() + "/api/images/view/" + fileName;
    }
    
    /**
     * URL에서 파일명 추출
     */
    private String extractFileNameFromUrl(String fileUrl) {
        if (fileUrl == null) return null;
        
        int lastSlashIndex = fileUrl.lastIndexOf('/');
        if (lastSlashIndex >= 0 && lastSlashIndex < fileUrl.length() - 1) {
            return fileUrl.substring(lastSlashIndex + 1);
        }
        return null;
    }
}
