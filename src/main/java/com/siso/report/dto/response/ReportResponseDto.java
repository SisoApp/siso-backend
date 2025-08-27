package com.siso.report.dto.response;

import com.siso.report.domain.model.Report;
import com.siso.report.domain.model.ReportType;

import java.time.LocalDateTime;

public record ReportResponseDto(
        Long id,
        Long reporterId,
        Long reportedId,
        String reportTitle,
        String description,
        LocalDateTime createdAt,
        ReportType reportType
) {
    public static ReportResponseDto fromEntity(Report report) {
        return new ReportResponseDto(
                report.getId(),
                report.getReporter().getId(),
                report.getReported().getId(),
                report.getReportTitle(),
                report.getDescription(),
                report.getCreatedAt(),
                report.getReportType()
        );
    }
}