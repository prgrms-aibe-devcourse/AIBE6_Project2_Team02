package com.backend.common.domain.project.application.entity;

public enum SelectionStatus {
    PENDING,   // 대기 중 (검토 중)
    ACCEPTED,  // 수락/합격
    REJECTED,  // 거절/불합격
    CANCELLED  // 지원자 또는 제안자가 스스로 취소
}
