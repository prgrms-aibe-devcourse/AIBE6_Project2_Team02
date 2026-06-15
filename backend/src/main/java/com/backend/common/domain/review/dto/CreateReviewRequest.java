package com.backend.common.domain.review.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateReviewRequest {
    @NotNull
    private Long projectId;

    @NotNull
    private Long revieweeId;

    @NotBlank
    @Size(max = 1000)
    private String content;
}
