package com.backend.common.domain.report.dto;

import com.backend.common.domain.report.entity.Report;
import com.backend.common.domain.report.enums.ReportReasonType;
import com.backend.common.domain.report.enums.ReportStatus;
import com.backend.common.domain.report.enums.ReportTargetType;

import java.time.LocalDateTime;

public record ReportResponse(
        Long reportId,
        Long reporterId,
        ReportTargetType targetType,
        Long targetId,
        ReportReasonType reasonType,
        String reasonDetail,
        ReportStatus status,
        LocalDateTime createdAt,
        LocalDateTime reviewedAt
) {

    public static ReportResponse from(Report report) {
        return new ReportResponse(
                report.getId(),
                report.getReporter().getId(),
                report.getTargetType(),
                report.getTargetId(),
                report.getReasonType(),
                report.getReasonDetail(),
                report.getStatus(),
                report.getCreatedAt(),
                report.getReviewedAt()
        );
    }
}
