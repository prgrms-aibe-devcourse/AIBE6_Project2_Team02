package com.backend.common.domain.project.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum PositionType {
    BACKEND("BACKEND", "백엔드 개발자"),
    FRONTEND("FRONTEND", "프론트엔드 개발자"),
    FULL_STACK("FULL_STACK", "풀스택 개발자"),
    DESIGNER("DESIGNER", "디자이너"),
    PRODUCT_MANAGER("PRODUCT_MANAGER", "프로덕트 매니저"),
    ERROR("error", "알 수 없는 포지션");

    private final String code;
    private final String description;

    public static PositionType fromDescriptionOrCode(String value) {
        if (value == null || value.trim().isEmpty()) {
            return ERROR;
        }

        String trimmed = value.trim();
        String normalized = normalize(trimmed);

        return Arrays.stream(PositionType.values())
                .filter(position -> position.name().equalsIgnoreCase(trimmed) ||
                        position.code.equalsIgnoreCase(trimmed) ||
                        position.description.equals(trimmed) ||
                        normalize(position.description).equals(normalized))
                .findFirst()
                .orElse(ERROR);
    }

    public String toRequiredFormat() {
        return description;
    }

    private static String normalize(String value) {
        return value == null ? "" : value.replaceAll("\\s+", "").toLowerCase();
    }
}
