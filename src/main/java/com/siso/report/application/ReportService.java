package com.siso.report.application;

import com.siso.report.domain.Report;
import com.siso.report.domain.ReportType;
import com.siso.report.dto.ReportResponseDto;
import com.siso.report.repository.ReportRepository;
import com.siso.report.requestDto.ReportRequestDto;
import com.siso.user.domain.model.User;
import com.siso.user.domain.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public ReportResponseDto findById(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("신고를 찾을 수 없습니다. id"));
        return ReportResponseDto.fromEntity(report);
    }

    // 전체 조회
    @Transactional(readOnly = true)
    public List<ReportResponseDto> findAll() {
        return reportRepository.findAll()
                .stream()
                .map(ReportResponseDto::fromEntity)
                .toList();
    }

    // 신고하기
    public ReportResponseDto addReport(ReportRequestDto reportRequestDto) {
        User reporter = userRepository.findById(reportRequestDto.getReporterId())
                .orElseThrow(() -> new EntityNotFoundException("신고자를 찾을 수 없습니다."));
        User reported = userRepository.findById(reportRequestDto.getReportedId())
                .orElseThrow(() -> new EntityNotFoundException("피신고자를 찾을 수 없습니다."));

        Report report = Report.builder()
                .reporter(reporter)
                .reported(reported)
                .reportTitle(reportRequestDto.getReportTitle())
                .reportType(reportRequestDto.getReportType())
                .description(reportRequestDto.getDescription())
                .createdAt(LocalDateTime.now())
                .build();

        reportRepository.save(report);
        return ReportResponseDto.fromEntity(report);
    }

    // 삭제
    public void delete(Long id) {
        if (!reportRepository.existsById(id)) {
            throw new EntityNotFoundException("신고를 찾을 수 없습니다. id");
        }
        reportRepository.deleteById(id);
    }

}