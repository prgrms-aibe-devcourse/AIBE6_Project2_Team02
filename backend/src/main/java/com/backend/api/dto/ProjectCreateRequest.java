package com.backend.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectCreateRequest {
    private String title;
    private String description;
    private List<String> goals;
    private String deadline;
    private boolean open;
    private Long leaderId;
    private List<Long> memberIds;
    private List<String> techStacks;
    private List<PositionCreateRequest> positions;
}
