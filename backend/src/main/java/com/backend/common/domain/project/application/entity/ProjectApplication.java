package com.backend.common.domain.project.application.entity;

import com.backend.common.domain.member.entity.Member;
import com.backend.common.domain.project.enums.PositionType;
import com.backend.common.domain.project.enums.SelectionStatus;
import com.backend.common.domain.project.project.entity.Project;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

// 프로젝트 지원 내역 -> 프로젝트 상세에서 지원
@Entity
@Table(name = "project_applications", indexes = {
        @Index(name = "idx_project_applicant", columnList = "project_id, applicant_id", unique = true)
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id")
    private Member applicant;

    @Enumerated(EnumType.STRING)
    private PositionType position;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    private SelectionStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * 지원 수락 (ACCEPTED)
     */
    public void accept() {
        if (this.status != SelectionStatus.PENDING) {
            throw new IllegalStateException("대기 중인 지원서만 수락할 수 있습니다.");
        }
        this.status = SelectionStatus.ACCEPTED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 지원 거절 (REJECTED)
     */
    public void reject() {
        if (this.status != SelectionStatus.PENDING) {
            throw new IllegalStateException("대기 중인 지원서만 거절할 수 있습니다.");
        }
        this.status = SelectionStatus.REJECTED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 지원 취소 (CANCELLED) - 지원자 본인이 철회할 때 사용
     */
    public void cancel() {
        if (this.status != SelectionStatus.PENDING) {
            throw new IllegalStateException("대기 중인 지원서만 취소할 수 있습니다.");
        }
        this.status = SelectionStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

}
