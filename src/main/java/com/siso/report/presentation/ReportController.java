package com.siso.report.presentation;

import com.siso.report.application.ReportService;
import com.siso.report.dto.ReportResponseDto;
import com.siso.report.requestDto.ReportRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/reports")
public class ReportController {
    private final ReportService reportService;

    // 단일 조회
    @Operation(summary = "신고 단일 조회",description = "신고 단일 조회")
    @GetMapping("/{id}")
    public ResponseEntity<ReportResponseDto> getReport(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(reportService.findById(id));
    }

    // 전체 조회
    @Operation(summary = "신고 전체 조회",description = "신고 전체 조회")
    @GetMapping
    public ResponseEntity<List<ReportResponseDto>> getAllReport() {
        return ResponseEntity.ok(reportService.findAll());
    }

    // 신고 하기
    @Operation(summary = "신고 등록",description = "신고 등록")
    @PostMapping
    public ResponseEntity<ReportResponseDto> addReport(@Valid @RequestBody ReportRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reportService.addReport(dto));
    }

    // 신고 삭제
    @Operation(summary = "신고 삭제",description = "신고 삭제")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> reportDelete(@PathVariable(name = "id") Long id) {
        reportService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
