package com.backend.api.dto;

import java.util.List;

public record ProjectCreateRequest(
        String title,
        String description,
        List<String>goals,
        String deadline,
        boolean open,
        Long leaderId,
        List<Long> memberIds,
        List<String> techStacks,
        List<PositionCreateRequest> positions
) {

}
