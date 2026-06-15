package com.backend.common.domain.report.service;

import com.backend.common.domain.member.entity.Member;
import com.backend.common.domain.member.repository.MemberRepository;
import com.backend.common.domain.report.dto.ReportResponse;
import com.backend.common.domain.report.entity.Report;
import com.backend.common.domain.report.enums.ReportStatus;
import com.backend.common.domain.report.enums.ReportTargetType;
import com.backend.common.domain.report.repository.ReportRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReportService {
    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;

    public List<ReportResponse> getReports(
            ReportTargetType targetType,
            ReportStatus status
    ) {

        List<Report> reports;

        if (targetType == null) {
            reports = reportRepository
                    .findByStatusOrderByCreatedAtDesc(status);
        } else {
            reports = reportRepository
                    .findByTargetTypeAndStatusOrderByCreatedAtDesc(
                            targetType,
                            status
                    );
        }

        return reports.stream()
                .map(ReportResponse::from)
                .toList();
    }

    @Transactional
    public void resolveReport(Long reportId, Long adminId) {

        Report report = reportRepository.findById(reportId)
                .orElseThrow(EntityNotFoundException::new);

        if (report.getStatus() != ReportStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 신고입니다.");
        }

        Member admin = memberRepository.findById(adminId)
                .orElseThrow(() ->
                        new IllegalArgumentException("존재하지 않는 관리자입니다."));

        report.resolve(admin);
    }

    @Transactional
    public void rejectReport(Long reportId, Long adminId) {

        Report report = reportRepository.findById(reportId)
                .orElseThrow(EntityNotFoundException::new);

        if (report.getStatus() != ReportStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 신고입니다.");
        }

        Member admin = memberRepository.findById(adminId)
                .orElseThrow(() ->
                        new IllegalArgumentException("존재하지 않는 관리자입니다."));

        report.reject(admin);
    }
}
