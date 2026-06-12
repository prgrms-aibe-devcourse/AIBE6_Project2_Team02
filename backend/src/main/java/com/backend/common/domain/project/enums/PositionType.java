package com.backend.common.domain.project.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PositionType {
    BACKEND("BACKEND", "백엔드"),
    FRONTEND("FRONTEND", "프론트엔드"),
    FULL_STACK("FULL_STACK", "풀스택"),
    DESIGNER("DESIGNER", "디자이너"),
    PRODUCT_MANAGER("PRODUCT_MANAGER", "기획자");

    private final String code;
    private final String description;
}
