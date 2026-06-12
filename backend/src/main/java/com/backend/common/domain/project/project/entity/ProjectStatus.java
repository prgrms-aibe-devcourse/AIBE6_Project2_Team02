package com.backend.common.domain.project.project.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProjectStatus {
    RECRUITING("RECRUITING", "모집 중"),
    IN_PROGRESS("IN_PROGRESS", "진행 중"),
    COMPLETED("COMPLETED", "완료"),
    DISBANDED("DISBANDED", "해산됨"),
    CANCELLED("CANCELLED", "취소됨");

    private final String code;
    private final String description;
}
