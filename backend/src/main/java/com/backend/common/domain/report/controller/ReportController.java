package com.backend.common.domain.report.controller;

import com.backend.common.domain.report.dto.CreateReportRequest;
import com.backend.common.domain.report.dto.CreateReportResponse;
import com.backend.common.domain.report.service.ReportService;
import com.backend.common.global.rsdata.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<RsData<CreateReportResponse>> createReport(
            @RequestParam Long reporterId,
            @RequestBody CreateReportRequest request
    ) {

        Long reportId = reportService.createReport(reporterId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        RsData.of(
                                "201",
                                "신고가 접수되었습니다.",
                                new CreateReportResponse(reportId)
                        )
                );
    }
}
