package com.backend.common.domain.project.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum RecruitmentStatus {
    RECRUITING("RECRUITING"),
    CLOSED("CLOSED"),
    COMPLETED("COMPLETED"),
    STOPPED("STOPPED");

    private final String value;

    RecruitmentStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
