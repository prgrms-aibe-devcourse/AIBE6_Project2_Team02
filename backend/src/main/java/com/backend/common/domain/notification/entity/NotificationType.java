package com.backend.common.domain.notification.entity;

public enum NotificationType {
    PROPOSAL_RECEIVED,   // 제안 받음 (대상: portfolio.member)
    PROPOSAL_ACCEPTED,   // 제안 수락됨 (대상: proposer)
    PROPOSAL_REJECTED,   // 제안 거절됨 (대상: proposer)
    APPLICATION_RECEIVED,// 지원 받음 (대상: 프로젝트 리더)
    APPLICATION_ACCEPTED,// 지원 수락됨 (대상: applicant)
    APPLICATION_REJECTED,// 지원 거절됨 (대상: applicant)
    REVIEW_RECEIVED,     // 리뷰 받음 (대상: reviewee)
    WELCOME              // 첫 가입 환영 (대상: 신규 가입자)
}
