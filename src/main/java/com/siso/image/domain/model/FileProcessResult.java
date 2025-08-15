package com.siso.image.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 파일 처리 결과를 담는 도메인 모델
 * 
 * 이미지 파일 업로드/처리 후 생성되는 결과 정보를 담는 불변 객체
 * ImageService의 processImageFile() 메서드에서 반환되는 값
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileProcessResult {
    
    /** 생성된 파일 접근 URL */
    private String fileUrl;
    
    /** 서버에 저장된 파일명 (UUID + 확장자) */
    private String serverImageName;
    
    /** 원본 파일명 (사용자가 업로드한 파일의 원래 이름) */
    private String originalName;
    
    /**
     * 정적 팩토리 메서드 - 파일 처리 결과 생성
     * 
     * @param fileUrl 파일 접근 URL
     * @param serverImageName 서버 파일명
     * @param originalName 원본 파일명
     * @return FileProcessResult 인스턴스
     */
    public static FileProcessResult of(String fileUrl, String serverImageName, String originalName) {
        return new FileProcessResult(fileUrl, serverImageName, originalName);
    }
    
    /**
     * 파일 처리가 성공했는지 확인
     * 
     * @return 모든 필드가 null이 아니고 비어있지 않으면 true
     */
    public boolean isValid() {
        return fileUrl != null && !fileUrl.trim().isEmpty() &&
               serverImageName != null && !serverImageName.trim().isEmpty() &&
               originalName != null && !originalName.trim().isEmpty();
    }
    
    /**
     * 파일 확장자 추출
     * 
     * @return 파일 확장자 (점 포함, 예: ".jpg")
     */
    public String getFileExtension() {
        if (serverImageName == null) {
            return "";
        }
        int lastDotIndex = serverImageName.lastIndexOf('.');
        return lastDotIndex > 0 ? serverImageName.substring(lastDotIndex) : "";
    }
}
