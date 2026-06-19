package com.backend.common.domain.project.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProjectStatus {
    RECRUITING("RECRUITING", "모집 중"),
    CLOSED("CLOSED", "인원 모집 마감"),
    IN_PROGRESS("IN_PROGRESS", "진행 중"),
    COMPLETED("COMPLETED", "완료"),
    DISBANDED("DISBANDED", "해산"),
    CANCELLED("CANCELLED", "취소");

    private final String code;
    private final String description;

    public static ProjectStatus fromString(String value) {
        for (ProjectStatus status : ProjectStatus.values()) {
            if (status.name().equalsIgnoreCase(value) || status.code.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("존재하지 않는 프로젝트 상태 값입니다: " + value);
    }
}
