package com.siso.report.application;

import com.siso.common.exception.ErrorCode;
import com.siso.common.exception.ExpectedException;
import com.siso.report.domain.model.Report;
import com.siso.report.dto.response.ReportResponseDto;
import com.siso.report.domain.repository.ReportRepository;
import com.siso.report.dto.request.ReportRequestDto;
import com.siso.user.domain.model.User;
import com.siso.user.domain.repository.UserRepository;
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
                .orElseThrow(() -> new ExpectedException(ErrorCode.REPORTER_NOT_FOUND));
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
    public ReportResponseDto addReport(User reporter, ReportRequestDto reportRequestDto) {
        User reported = userRepository.findById(reportRequestDto.getReportedId())
                .orElseThrow(() -> new ExpectedException(ErrorCode.REPORTED_USER_NOT_FOUND));

        Report report = Report.builder()
                .reporter(reporter)
                .reported(reported)
                .reportTitle(reportRequestDto.getReportTitle())
                .reportType(reportRequestDto.getReportType())
                .description(reportRequestDto.getDescription())
                .build();

        reportRepository.save(report);
        return ReportResponseDto.fromEntity(report);
    }

    // 삭제
    public void delete(Long id) {
        if (!reportRepository.existsById(id)) {
            throw new ExpectedException(ErrorCode.REPORTER_NOT_FOUND);
        }
        reportRepository.deleteById(id);
    }

}