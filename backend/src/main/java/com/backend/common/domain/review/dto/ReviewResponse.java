package com.backend.common.domain.review.dto;

import com.backend.common.domain.review.entity.Review;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReviewResponse {

    private Long reviewId;

    private Long projectId;

    private Long reviewerId;
    private String reviewerNickname;

    private Long revieweeId;
    private String revieweeNickname;

    private String content;

    private LocalDateTime createdAt;

    public static ReviewResponse from(Review review) {
        return ReviewResponse.builder()
                .reviewId(review.getId())
                .projectId(review.getProject().getId())
                .reviewerId(review.getReviewer().getId())
                .reviewerNickname(review.getReviewer().getNickname())
                .revieweeId(review.getReviewee().getId())
                .revieweeNickname(review.getReviewee().getNickname())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
