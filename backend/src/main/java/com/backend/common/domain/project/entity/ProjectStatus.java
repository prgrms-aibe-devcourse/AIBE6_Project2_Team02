package com.backend.common.domain.project.entity;

public enum ProjectStatus {
    RECRUITING,   // 팀원 모집 중
    IN_PROGRESS,  // 프로젝트 진행 중
    COMPLETED,    // 프로젝트 완료
    DISBANDED,    // 중도 폭파/해산 (데이터 보존용)
    CANCELLED     // 시작 전 취소
}
