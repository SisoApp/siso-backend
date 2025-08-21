package com.siso.report.requestDto;

import com.siso.report.domain.ReportType;
import lombok.Getter;

@Getter
public class ReportRequestDto {
    private Long reporterId;
    private Long reportedId;
    private String reportTitle;
    private String description;
    private ReportType reportType;
}
