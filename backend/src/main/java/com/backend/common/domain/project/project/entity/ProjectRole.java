package com.backend.common.domain.project.project.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProjectRole {
    // 하위 권한은 상위 권한도 사용 가능
    LEADER("LEADER", "팀 리더"), // 방출 , 프로젝트 상태변경, 지원자 승인거절
    MANAGER("MANAGER", "부리더"), // 프로젝트 내용수정,
    MEMBER("MEMBER", "일반 팀원"); // 프로젝트 제안

    private final String code;
    private final String description;
}
